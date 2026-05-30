package org.example;

final class GitHubUserNotFoundException extends RuntimeException {

    GitHubUserNotFoundException(String message) {
        super(message);
    }
}
