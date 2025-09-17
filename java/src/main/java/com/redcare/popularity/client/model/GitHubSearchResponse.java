package com.redcare.popularity.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GitHubSearchResponse(
        @JsonProperty("total_count") long totalCount,
        List<GitHubRepoItem> items
) {}
