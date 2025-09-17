package com.redcare.popularity.service;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ScoringService {

    public double computeScore(int stars, int forks, OffsetDateTime updatedAt) {
        long days = Math.max(0, updatedAt.until(OffsetDateTime.now(), ChronoUnit.DAYS));
        double recency = 100.0 * (1.0 / (1.0 + (days / 30.0)));
        return 0.6 * stars + 0.3 * forks + 0.1 * recency;
    }
}
