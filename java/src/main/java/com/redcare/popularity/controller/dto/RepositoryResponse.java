package com.redcare.popularity.controller.dto;

import java.time.OffsetDateTime;

public record RepositoryResponse(
        long id,
        String name,
        String htmlUrl,
        String language,
        int stars,
        int forks,
        OffsetDateTime updatedAt,
        double score
) {}

