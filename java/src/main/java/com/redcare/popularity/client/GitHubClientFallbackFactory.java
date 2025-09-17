package com.redcare.popularity.client;

import com.redcare.popularity.exception.GitHubApiException;
import org.springframework.stereotype.Component;

@Component
public class GitHubClientFallbackFactory implements org.springframework.cloud.openfeign.FallbackFactory<GitHubClient> {
    @Override
    public GitHubClient create(Throwable cause) {
        return (q, perPage, page) -> { throw new GitHubApiException(503, "Circuit/Fallback", cause); };
    }
}
