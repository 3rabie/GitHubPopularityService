package com.redcare.popularity.controller;

import com.redcare.popularity.controller.dto.PopularityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;

@Tag(name = "Popularity", description = "Repository popularity APIs")
public interface PopularityApi {

    @Operation(
            summary = "List repositories by popularity",
            description = "Search GitHub repositories and return items ranked by a popularity score."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.redcare.popularity.controller.dto.PopularityResponse.class),
                            examples = @ExampleObject(name = "success",
                                    value = "{\n  \"totalCount\": 2,\n  \"count\": 2,\n  \"items\": [\n    {\n      \"id\": 101,\n      \"name\": \"a/a\",\n      \"htmlUrl\": \"https://x/1\",\n      \"language\": \"Java\",\n      \"stars\": 10,\n      \"forks\": 5,\n      \"updatedAt\": \"2024-01-01T00:00:00Z\",\n      \"score\": 17.5\n    },\n    {\n      \"id\": 102,\n      \"name\": \"b/b\",\n      \"htmlUrl\": \"https://x/2\",\n      \"language\": \"Java\",\n      \"stars\": 5,\n      \"forks\": 10,\n      \"updatedAt\": \"2024-01-01T00:00:00Z\",\n      \"score\": 15.5\n    }\n  ]\n}"))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.redcare.popularity.exception.ErrorResponse.class),
                            examples = @ExampleObject(name = "bad-request",
                                    value = "{\n  \"status\": 400,\n  \"error\": \"Bad Request\",\n  \"message\": \"Invalid request. Please review your parameters.\"\n}"))),
            @ApiResponse(responseCode = "429", description = "Rate limited",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.redcare.popularity.exception.ErrorResponse.class),
                            examples = @ExampleObject(name = "rate-limited",
                                    value = "{\n  \"status\": 429,\n  \"error\": \"Too Many Requests\",\n  \"message\": \"GitHub rate limit reached. Try again later.\"\n}"))),
            @ApiResponse(responseCode = "502", description = "Upstream error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.redcare.popularity.exception.ErrorResponse.class),
                            examples = @ExampleObject(name = "bad-gateway",
                                    value = "{\n  \"status\": 502,\n  \"error\": \"Bad Gateway\",\n  \"message\": \"Temporary upstream issue. Please try again.\"\n}"))),
            @ApiResponse(responseCode = "503", description = "Circuit open",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = com.redcare.popularity.exception.ErrorResponse.class),
                            examples = @ExampleObject(name = "circuit-open",
                                    value = "{\n  \"status\": 503,\n  \"error\": \"Service Unavailable\",\n  \"message\": \"Service temporarily unavailable. Please try again shortly.\"\n}")))
    })
    PopularityResponse popularity(
            @Parameter(description = "Optional language filter, e.g. Java") String language,
            @Parameter(description = "Optional ISO date (yyyy-MM-dd) to include repos created on or after the date") LocalDate createdAfter,
            @Parameter(description = "Optional free-text search forwarded to GitHub") String query,
            @Parameter(description = "Page size (1..100)") @Min(1) @Max(100) int perPage,
            @Parameter(description = "Page number (>=1)") @Min(1) int page
    );
}
