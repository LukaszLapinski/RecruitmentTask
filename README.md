# RecruitmentTask
The application exposes REST endpoint. It returns non-fork GitHub repositories with branches and last commit SHA.

# Tech Stack
- Java 21
- Spring Boot 3.5
- Gradle (Kotlin DSL)
- Spring Web
- JUnit 5
- WireMock (for integration testing)

# Installation
1. Clone the repository (git clone https://github.com/LukaszLapinski/RecruitmentTask   --->  cd RecruitmentTask1)
2. Build the project (./gradlew build)
3. Run the application (./gradlew bootRun)
The application will start on: http://localhost:8080

# Features
- Get repositories for a GitHub user
- Returns all repositories for a given user excluding forked repositories
- Each repository includes: repository name, owner login, branch name, last commit SHA

# API Endpoint
GET /users/{username}/repositories
