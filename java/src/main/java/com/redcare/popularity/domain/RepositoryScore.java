package com.redcare.popularity.domain;

import java.time.OffsetDateTime;

public record RepositoryScore(
        long id,
        String name,
        String htmlUrl,
        String language,
        int stars,
        int forks,
        OffsetDateTime updatedAt,
        double score
) {}

