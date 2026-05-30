package org.example;

public record BranchDto(
        String name,
        String lastCommitSha
) {}
