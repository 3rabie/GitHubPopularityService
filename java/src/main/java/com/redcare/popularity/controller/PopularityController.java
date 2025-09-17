package com.redcare.popularity.controller;

import com.redcare.popularity.controller.dto.PopularityResponse;
import com.redcare.popularity.service.PopularityService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequestMapping(path = "/repos", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PopularityController implements PopularityApi {
    private final PopularityService popularityService;

    @Override
    @GetMapping("/popularity")
    public PopularityResponse popularity(
            @RequestParam(name = "language", required = false) String language,
            @RequestParam(name = "created_after", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAfter,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "per_page", required = false, defaultValue = "20") int perPage,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page
    ) {
        return popularityService.searchAndScore(language, createdAfter, query, perPage, page);
    }
}
