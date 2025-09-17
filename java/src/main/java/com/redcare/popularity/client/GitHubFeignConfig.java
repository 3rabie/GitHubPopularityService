package com.redcare.popularity.client;

import com.redcare.popularity.exception.GitHubApiException;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GitHubFeignConfig {

    @Value("${github.token:}")
    private String token;

    @Bean
    public RequestInterceptor githubHeadersInterceptor() {
        return template -> {
            template.header(HttpHeaders.ACCEPT, "application/vnd.github+json");
            template.header("X-GitHub-Api-Version", "2022-11-28");
            template.header(HttpHeaders.USER_AGENT, "redcare-popularity/0.1");
            if (token != null && !token.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            }
        };
    }

    @Bean
    public ErrorDecoder githubErrorDecoder() {
        return (methodKey, response) -> new GitHubApiException(normalizeStatus(response), response.reason(), null);
    }

    private static int normalizeStatus(Response response) {
        int status = response.status();
        if (status == 429) return 429;
        if (status == 403 && isRateLimitBody(response)) return 429;
        return status;
    }

    private static boolean isRateLimitBody(Response response) {
        try {
            if (response.body() == null) return false;
            try (InputStream is = response.body().asInputStream()) {
                String body = new String(is.readAllBytes(), StandardCharsets.UTF_8).toLowerCase();
                return body.contains("rate limit");
            }
        } catch (Exception ignored) {
            return false;
        }
    }
}
