package com.redcare.popularity.util;

public class CommonUtils {
    public static double roundScore(double scoreValue) {
        return Math.round(scoreValue * 100.0) / 100.0;
    }
}
