package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
final class GitHubClient {

    private final RestClient restClient;

    GitHubClient(@Value("${github.api-base-url:https://api.github.com}") String githubApiBaseUrl) {
        this.restClient = RestClient.create(githubApiBaseUrl);
    }

    List<GitHubRepo> getRepositories(String username) {

        try {
            return restClient.get()
                    .uri("/users/{username}/repos", username)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepo>>() {});

        } catch (HttpClientErrorException.NotFound ex) {
            throw new GitHubUserNotFoundException("GitHub user not found: " + username);
        }
    }

    List<GitHubBranch> getBranches(String owner, String repo) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repo)
                .retrieve()
                .body(new ParameterizedTypeReference<List<GitHubBranch>>() {});
    }
}
