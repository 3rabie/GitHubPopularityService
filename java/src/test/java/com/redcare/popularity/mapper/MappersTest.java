package com.redcare.popularity.mapper;

import com.redcare.popularity.controller.dto.PopularityResponse;
import com.redcare.popularity.controller.dto.RepositoryResponse;
import com.redcare.popularity.domain.PopularityResult;
import com.redcare.popularity.domain.RepositoryScore;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MappersTest {

    @Test
    void shouldMapDomainToControllerDtosWhenConverting() {
        var rs = List.of(
                new RepositoryScore(1, "a/a", "u1", "Java", 1, 2, OffsetDateTime.now(), 12.34),
                new RepositoryScore(2, "b/b", "u2", "Kotlin", 3, 4, OffsetDateTime.now(), 56.78)
        );
        var domain = new PopularityResult(99, rs);

        PopularityResponse dto = Mappers.toPopularityResponse(domain);

        assertEquals(99, dto.totalCount());
        assertEquals(2, dto.count());
        assertEquals(2, dto.items().size());

        RepositoryResponse r0 = dto.items().get(0);
        assertEquals(1, r0.id());
        assertEquals("a/a", r0.name());
        assertEquals("u1", r0.htmlUrl());
        assertEquals("Java", r0.language());
        assertEquals(1, r0.stars());
        assertEquals(2, r0.forks());
        assertEquals(12.34, r0.score());
    }

    @Test
    void shouldMapUpstreamItemToDomainWithRoundedScore() {
        var now = OffsetDateTime.now();
        var upstream = new com.redcare.popularity.client.model.GitHubRepoItem(
                99, "z/z", "https://x/z", "Java", 7, 3, now
        );
        // Provide a score that would round to 12.35
        double raw = 12.3456;

        RepositoryScore domain = Mappers.toRepositoryScore(upstream, raw);

        assertEquals(99, domain.id());
        assertEquals("z/z", domain.name());
        assertEquals("https://x/z", domain.htmlUrl());
        assertEquals("Java", domain.language());
        assertEquals(7, domain.stars());
        assertEquals(3, domain.forks());
        assertEquals(now, domain.updatedAt());
        assertEquals(12.35, domain.score());
    }
}
