package com.redcare.popularity.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.cloud.compatibility-verifier.enabled=false",
        "github.retry.maxAttempts=1"
})
@AutoConfigureMockMvc
class PopularityControllerEndToEndTest {

    private static WireMockServer wm;

    @BeforeAll
    static void setup() {
        wm = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wm.start();
        WireMock.configureFor("localhost", wm.port());
    }

    @AfterAll
    static void teardown() {
        wm.stop();
    }

    @DynamicPropertySource
    static void shouldPointFeignToWireMock(DynamicPropertyRegistry registry) {
        registry.add("github.apiBaseUrl", () -> wm.baseUrl());
        registry.add("github.token", () -> "test-token-123");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnSortedItemsAndForwardHeadersWhenSearching() throws Exception {
        // Given
        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("framework language:Java created:>=2024-01-01"))
                .withQueryParam("per_page", equalTo("20"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(okJson("{" +
                        "\"total_count\":2,\n" +
                        "\"items\":[{" +
                        "\"id\": 101, \"full_name\": \"a/a\", \"html_url\": \"https://x/1\", \"language\": \"Java\", \"stargazers_count\": 10, \"forks_count\": 5, \"updated_at\": \"2024-01-01T00:00:00Z\"},{" +
                        "\"id\": 102, \"full_name\": \"b/b\", \"html_url\": \"https://x/2\", \"language\": \"Java\", \"stargazers_count\": 5, \"forks_count\": 10, \"updated_at\": \"2024-01-01T00:00:00Z\"}" +
                        "]}")));

        // When
        String json = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .param("language", "Java")
                        .param("created_after", "2024-01-01")
                        .param("query", "framework")
                        .param("per_page", "20")
                        .param("page", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.count").value(2))
                .andReturn().getResponse().getContentAsString();

        // Then
        JsonNode root = objectMapper.readTree(json);
        assertEquals(2, root.get("items").size());
        double s0 = root.get("items").get(0).get("score").asDouble();
        double s1 = root.get("items").get(1).get("score").asDouble();
        assertThat(s0, greaterThanOrEqualTo(s1));

        verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withHeader("Accept", equalTo("application/vnd.github+json"))
                .withHeader("X-GitHub-Api-Version", equalTo("2022-11-28"))
                .withHeader("Authorization", equalTo("Bearer test-token-123"))
                .withHeader("User-Agent", equalTo("redcare-popularity/0.1")));
    }

    @Test
    void shouldReturn502AfterRetryWhenUpstreamKeepsFailing() throws Exception {
        wm.resetAll();
        final String scenario = "retry-then-fail";
        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .inScenario(scenario)
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("again"));

        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .inScenario(scenario)
                .whenScenarioStateIs("again")
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .param("query", "bar")
                        .param("per_page", "5")
                        .param("page", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway());

        verify(1, getRequestedFor(urlPathEqualTo("/search/repositories")));
    }

    @Test
    void shouldUseDefaultPagingWhenParamsMissing() throws Exception {
        wm.resetAll();
        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .withQueryParam("q", equalTo("abc"))
                .withQueryParam("per_page", equalTo("20"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(okJson("{\"total_count\":0,\"items\":[]}")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .param("query", "abc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.count").value(0));

        verify(getRequestedFor(urlPathEqualTo("/search/repositories"))
                .withQueryParam("per_page", equalTo("20"))
                .withQueryParam("page", equalTo("1")));
    }

    @Test
    void shouldReturn429WhenUpstreamIsRateLimited() throws Exception {
        wm.resetAll();
        final String scenario = "rate-limit";
        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .inScenario(scenario)
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse().withStatus(429).withBody("{\"message\":\"rate limited\"}"))
                .willSetStateTo("again"));
        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .inScenario(scenario)
                .whenScenarioStateIs("again")
                .willReturn(aResponse().withStatus(429).withBody("{\"message\":\"rate limited\"}")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(429));

        verify(1, getRequestedFor(urlPathEqualTo("/search/repositories")));
    }

    @Test
    void shouldReturn400WhenPerPageIsOutOfRange() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .param("per_page", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturn400WhenPageIsLessThanOne() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .param("page", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void shouldReturn429WhenUpstreamIs403RateLimitedBody() throws Exception {
        wm.resetAll();
        wm.stubFor(get(urlPathEqualTo("/search/repositories"))
                .willReturn(aResponse().withStatus(403).withBody("{\"message\":\"API rate limit exceeded\"}")));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/repos/popularity")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(429));
    }
}
