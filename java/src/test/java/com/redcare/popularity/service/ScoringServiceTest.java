package com.redcare.popularity.service;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoringServiceTest {

    private final ScoringService scoring = new ScoringService();

    @Test
    void shouldHaveRecency100WhenUpdatedToday() {
        OffsetDateTime now = OffsetDateTime.now();
        double score = scoring.computeScore(0, 0, now);
        assertEquals(10.0, score, 1e-9);
    }

    @Test
    void shouldHaveRecency50WhenUpdated30DaysAgo() {
        OffsetDateTime now = OffsetDateTime.now();
        double score = scoring.computeScore(0, 0, now.minusDays(30));
        assertEquals(5.0, score, 1e-9);
    }

    @Test
    void shouldHaveRecency33_33WhenUpdated60DaysAgo() {
        OffsetDateTime now = OffsetDateTime.now();
        double score = scoring.computeScore(0, 0, now.minusDays(60));
        assertEquals(100.0 * (1.0 / (1.0 + (60.0 / 30.0))) * 0.1, score, 1e-9);
    }

    @Test
    void shouldClampRecencyTo100WhenUpdatedInFuture() {
        OffsetDateTime now = OffsetDateTime.now();
        double score = scoring.computeScore(0, 0, now.plusDays(10));
        assertEquals(10.0, score, 1e-9);
    }

    @Test
    void shouldApplyWeightsToStarsAndForksWhenScoring() {
        OffsetDateTime now = OffsetDateTime.now();
        double score = scoring.computeScore(10, 5, now);
        // 0.6*10 + 0.3*5 + 0.1*100 = 6 + 1.5 + 10 = 17.5
        assertEquals(17.5, score, 1e-9);
    }
}
