package org.example;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubControllerIT {

    private static final WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int port;

    @DynamicPropertySource
    static void registerGithubApi(DynamicPropertyRegistry registry) {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
        registry.add("github.api-base-url", wireMockServer::baseUrl);
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranches() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo("/users/octocat/repos"))
                .willReturn(okJson("""
                        [
                          {
                            "name": "repo1",
                            "fork": false,
                            "owner": {
                              "login": "octocat"
                            }
                          },
                          {
                            "name": "forked-repo",
                            "fork": true,
                            "owner": {
                              "login": "octocat"
                            }
                          }
                        ]
                        """)));

        wireMockServer.stubFor(get(urlEqualTo("/repos/octocat/repo1/branches"))
                .willReturn(okJson("""
                        [
                          {
                            "name": "main",
                            "commit": {
                              "sha": "main-sha"
                            }
                          },
                          {
                            "name": "develop",
                            "commit": {
                              "sha": "develop-sha"
                            }
                          }
                        ]
                        """)));

        HttpResponse<String> response = sendGet("/users/octocat/repositories");

        assertThat(response.statusCode()).isEqualTo(200);

        JSONAssert.assertEquals("""
                [
                  {
                    "repositoryName": "repo1",
                    "ownerLogin": "octocat",
                    "branches": [
                      {
                        "name": "main",
                        "lastCommitSha": "main-sha"
                      },
                      {
                        "name": "develop",
                        "lastCommitSha": "develop-sha"
                      }
                    ]
                  }
                ]
                """, response.body(), JSONCompareMode.STRICT);

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/octocat/repos")));
        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/repos/octocat/repo1/branches")));
        wireMockServer.verify(0, getRequestedFor(urlEqualTo("/repos/octocat/forked-repo/branches")));
    }

    @Test
    void shouldReturnNotFoundResponseForNotExistingGithubUser() throws Exception {
        wireMockServer.stubFor(get(urlEqualTo("/users/not-existing-user/repos"))
                .willReturn(notFound()));

        HttpResponse<String> response = sendGet("/users/not-existing-user/repositories");

        assertThat(response.statusCode()).isEqualTo(404);

        JSONAssert.assertEquals("""
                {
                  "status": 404,
                  "message": "GitHub user not found: not-existing-user"
                }
                """, response.body(), JSONCompareMode.STRICT);

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/users/not-existing-user/repos")));
    }

    private HttpResponse<String> sendGet(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
