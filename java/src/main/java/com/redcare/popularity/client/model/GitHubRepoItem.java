package com.redcare.popularity.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubRepoItem(
        long id,
        @JsonProperty("full_name") String fullName,
        @JsonProperty("html_url") String htmlUrl,
        String language,
        @JsonProperty("stargazers_count") int stargazersCount,
        @JsonProperty("forks_count") int forksCount,
        @JsonProperty("updated_at") OffsetDateTime updatedAt
) {}
