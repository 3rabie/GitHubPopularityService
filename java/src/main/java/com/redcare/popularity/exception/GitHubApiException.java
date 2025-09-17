package com.redcare.popularity.exception;

import lombok.Getter;

@Getter
public class GitHubApiException extends RuntimeException {
    private final int statusCode;

    public GitHubApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}

