package com.redcare.popularity.controller.dto;

import java.util.List;

public record PopularityResponse(
        long totalCount,
        int count,
        List<RepositoryResponse> items
) {}
