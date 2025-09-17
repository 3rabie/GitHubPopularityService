package com.redcare.popularity.domain;

import java.util.List;

public record PopularityResult(
        long totalCount,
        List<RepositoryScore> items
) {}

