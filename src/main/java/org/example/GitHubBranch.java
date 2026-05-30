package org.example;

record GitHubBranch(
        String name,
        Commit commit
) {
    record Commit(String sha) {}
}
