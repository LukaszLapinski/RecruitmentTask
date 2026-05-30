package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
final class GitHubController {

    private final GitHubService service;

    GitHubController(GitHubService service) {
        this.service = service;
    }

    @GetMapping("/{username}/repositories")
    List<RepositoryDto> getRepositories(@PathVariable String username) {
        return service.getRepositories(username);
    }
}
