package com.redcare.popularity.client;

import com.redcare.popularity.client.model.GitHubSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "github",
        url = "${github.apiBaseUrl:https://api.github.com}",
        configuration = GitHubFeignConfig.class,
        fallbackFactory = GitHubClientFallbackFactory.class
)
public interface GitHubClient {

    @GetMapping("/search/repositories")
    GitHubSearchResponse searchRepositories(
            @RequestParam("q") String q,
            @RequestParam("per_page") int perPage,
            @RequestParam("page") int page
    );
}
