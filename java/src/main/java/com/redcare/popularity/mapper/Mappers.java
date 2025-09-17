package com.redcare.popularity.mapper;

import com.redcare.popularity.client.model.GitHubRepoItem;
import com.redcare.popularity.controller.dto.PopularityResponse;
import com.redcare.popularity.controller.dto.RepositoryResponse;
import com.redcare.popularity.domain.PopularityResult;
import com.redcare.popularity.domain.RepositoryScore;

import java.util.List;

import static com.redcare.popularity.util.CommonUtils.roundScore;

public final class Mappers {
    private Mappers() {
    }

    public static RepositoryScore toRepositoryScore(GitHubRepoItem it, double score) {
        return new RepositoryScore(
                it.id(),
                it.fullName(),
                it.htmlUrl(),
                it.language(),
                it.stargazersCount(),
                it.forksCount(),
                it.updatedAt(),
                roundScore(score)
        );
    }

    public static PopularityResponse toPopularityResponse(PopularityResult result) {
        List<RepositoryResponse> items = result.items().stream()
                .map(it -> new RepositoryResponse(
                        it.id(),
                        it.name(),
                        it.htmlUrl(),
                        it.language(),
                        it.stars(),
                        it.forks(),
                        it.updatedAt(),
                        it.score()
                )).toList();
        return new PopularityResponse(result.totalCount(), items.size(), items);
    }

}
