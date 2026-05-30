package org.example;

record GitHubRepo(
        String name,
        boolean fork,
        Owner owner
) {
    record Owner(String login) {}
}
