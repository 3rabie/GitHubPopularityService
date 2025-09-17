package com.redcare.popularity.service;

import com.redcare.popularity.client.GitHubClient;
import com.redcare.popularity.client.model.GitHubRepoItem;
import com.redcare.popularity.client.model.GitHubSearchResponse;
import com.redcare.popularity.controller.dto.PopularityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PopularityServiceTest {

    private GitHubClient gitHubClient;
    private ScoringService scoringService;
    private PopularityService popularityService;

    @BeforeEach
    void setUp() {
        gitHubClient = mock(GitHubClient.class);
        scoringService = new ScoringService();
        popularityService = new PopularityService(gitHubClient, scoringService);
    }

    @Test
    void shouldBuildQueryWhenAllParametersProvided() {
        when(gitHubClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubSearchResponse(0, List.of()));

        popularityService.searchAndScore("Java", java.time.LocalDate.of(2024, 1, 1), "framework", 20, 1);

        ArgumentCaptor<String> qCaptor = ArgumentCaptor.forClass(String.class);
        verify(gitHubClient).searchRepositories(qCaptor.capture(), eq(20), eq(1));
        assertEquals("framework language:Java created:>=2024-01-01", qCaptor.getValue());
    }

    @Test
    void shouldBuildQueryWhenOnlyLanguageProvided() {
        when(gitHubClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubSearchResponse(0, List.of()));

        popularityService.searchAndScore("Kotlin", null, null, 10, 2);
        ArgumentCaptor<String> qCaptor = ArgumentCaptor.forClass(String.class);
        verify(gitHubClient).searchRepositories(qCaptor.capture(), eq(10), eq(2));
        assertEquals("language:Kotlin", qCaptor.getValue());
    }

    @Test
    void shouldBuildQueryWhenOnlyCreatedAfterProvided() {
        when(gitHubClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubSearchResponse(0, List.of()));

        popularityService.searchAndScore(null, java.time.LocalDate.of(2023, 1, 1), null, 5, 3);
        ArgumentCaptor<String> qCaptor = ArgumentCaptor.forClass(String.class);
        verify(gitHubClient).searchRepositories(qCaptor.capture(), eq(5), eq(3));
        assertEquals("created:>=2023-01-01", qCaptor.getValue());
    }

    @Test
    void shouldBuildQueryWhenOnlySearchQueryProvided() {
        when(gitHubClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubSearchResponse(0, List.of()));

        popularityService.searchAndScore(null, null, "reactive", 15, 4);
        ArgumentCaptor<String> qCaptor = ArgumentCaptor.forClass(String.class);
        verify(gitHubClient).searchRepositories(qCaptor.capture(), eq(15), eq(4));
        assertEquals("reactive", qCaptor.getValue());
    }

    @Test
    void shouldMapSortAndRoundScoresWhenComputingPopularity() {
        OffsetDateTime now = OffsetDateTime.now();
        var items = List.of(
                new GitHubRepoItem(1, "a/a", "https://x/1", "Java", 50, 10, now.minusDays(5)),
                new GitHubRepoItem(2, "b/b", "https://x/2", "Java", 5, 1, now.minusDays(60)),
                new GitHubRepoItem(3, "c/c", "https://x/3", "Kotlin", 30, 20, now.minusDays(1))
        );
        when(gitHubClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubSearchResponse(1234, items));

        PopularityResponse result = popularityService.searchAndScore(null, null, null, 20, 1);

        assertEquals(1234, result.totalCount());
        assertEquals(3, result.items().size());

        // Ensure sorted by score descending
        double s0 = result.items().get(0).score();
        double s1 = result.items().get(1).score();
        double s2 = result.items().get(2).score();
        assertTrue(s0 >= s1 && s1 >= s2);

        // Ensure rounding applied to 60-days item (~3.33 when stars/forks 0; here stars/forks non-zero)
        // We can specifically verify rounding behavior by constructing an item with 0 stars/forks and 60 days
        var items2 = List.of(new GitHubRepoItem(9, "z/z", "https://x/9", "Java", 0, 0, now.minusDays(60)));
        when(gitHubClient.searchRepositories(anyString(), anyInt(), anyInt()))
                .thenReturn(new GitHubSearchResponse(1, items2));
        PopularityResponse roundingRes = popularityService.searchAndScore(null, null, null, 1, 1);
        assertEquals(1, roundingRes.items().size());
        assertEquals(3.33, roundingRes.items().get(0).score(), 1e-9);
    }
}
