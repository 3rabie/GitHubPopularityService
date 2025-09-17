package com.redcare.popularity.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonProperty("status") int status,
        @JsonProperty("error") String error,
        @JsonProperty("message") String message
) {}
