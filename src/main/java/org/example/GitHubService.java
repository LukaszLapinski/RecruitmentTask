package org.example;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
final class GitHubService {

    private final GitHubClient client;

    GitHubService(GitHubClient client) {
        this.client = client;
    }

    List<RepositoryDto> getRepositories(String username) {

        return client.getRepositories(username).stream()

                .filter(repo -> !repo.fork())

                .map(repo -> {

                    String owner = repo.owner().login();

                    List<BranchDto> branches =
                            client.getBranches(owner, repo.name()).stream()
                                    .map(branch ->
                                            new BranchDto(
                                                    branch.name(),
                                                    branch.commit().sha()
                                            )
                                    )
                                    .toList();

                    return new RepositoryDto(
                            repo.name(),
                            owner,
                            branches
                    );
                })
                .toList();
    }
}
