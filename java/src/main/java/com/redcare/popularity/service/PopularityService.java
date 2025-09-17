package com.redcare.popularity.service;

import com.redcare.popularity.client.GitHubClient;
import com.redcare.popularity.client.model.GitHubRepoItem;
import com.redcare.popularity.client.model.GitHubSearchResponse;
import com.redcare.popularity.controller.dto.PopularityResponse;
import com.redcare.popularity.domain.PopularityResult;
import com.redcare.popularity.domain.RepositoryScore;
import com.redcare.popularity.mapper.Mappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularityService {
    private final GitHubClient gitHubClient;
    private final ScoringService scoringService;

    public PopularityResponse searchAndScore(String language, LocalDate createdAfter, String query,
                                             int perPage, int page) {
        log.atInfo().log("Start searching popular repositories");

        String q = buildQuery(language, createdAfter, query);
        GitHubSearchResponse resp = gitHubClient.searchRepositories(q, perPage, page);
        log.atDebug().log("Mapping {} items (GitHub total={}) for q='{}'", resp.items().size(), resp.totalCount(), q);
        List<RepositoryScore> items = resp.items().stream()
                .map(it -> Mappers.toRepositoryScore(it, computeScore(it)))
                .sorted(Comparator.comparingDouble(RepositoryScore::score).reversed())
                .toList();

        log.atInfo().log("Finished searching popular repositories");
        return Mappers.toPopularityResponse(new PopularityResult(resp.totalCount(), items));
    }

    private static String buildQuery(String language, LocalDate createdAfter, String query) {
        var joiner = new StringJoiner(" ");

        if (query != null && !query.isBlank()) joiner.add(query.trim());
        if (language != null && !language.isBlank()) joiner.add("language:" + language.trim());
        if (createdAfter != null) joiner.add("created:>=" + createdAfter);

        return joiner.toString();
    }

    private double computeScore(GitHubRepoItem gitHubRepoItem) {
        return scoringService.computeScore(gitHubRepoItem.stargazersCount(), gitHubRepoItem.forksCount(), gitHubRepoItem.updatedAt());
    }
}
