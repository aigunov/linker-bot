package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.GitHubApiException;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@TestPropertySource(properties = {"app.github.url=http://localhost:8089/repos", "app.github.token=test-token"})
class GitHubClientTest {

    @Autowired
    @Qualifier("gitHubClient")
    private GitHubClient gitHubClient;

    @Autowired
    private LinkToApiRequestConverter converterApi;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        // arrange
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor(8089);
    }

    @AfterEach
    void tearDown() {
        // arrange
        wireMockServer.stop();
    }

    @Test
    void checkUpdates_shouldReturnUpdatedAt() throws Exception {
        // arrange
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        String expectedUpdatedAtStr =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(expectedUpdatedAt.atOffset(ZoneOffset.UTC));

        String responseBody = "{\"updated_at\":\"" + expectedUpdatedAtStr + "\"}";

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody)));

        // act
        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        // assert
        assertThat(actualUpdatedAt).isPresent();
        assertThat(actualUpdatedAt.get()).isEqualTo(expectedUpdatedAt);
    }

    @Test
    void checkUpdates_success() {
        // arrange
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiPath = "/repos/testuser/testrepo";
        String updatedAt = "2023-10-27T10:00:00Z";
        String responseBody = "{\"updated_at\":\"" + updatedAt + "\"}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        // act
        Optional<LocalDateTime> result = gitHubClient.checkUpdates(githubUrl);

        // assert
        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.parse(updatedAt, DateTimeFormatter.ISO_DATE_TIME), result.get());
    }

    @Test
    void checkUpdates_clientError() {
        // arrange
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiPath = "/repos/testuser/testrepo";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse().withStatus(404)));

        // act & assert
        assertThrows(GitHubApiException.class, () -> gitHubClient.checkUpdates(githubUrl));
    }

    @Test
    void checkUpdates_serverError() {
        // arrange
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiPath = "/repos/testuser/testrepo";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse().withStatus(500)));

        // act & assert
        assertThrows(GitHubApiException.class, () -> gitHubClient.checkUpdates(githubUrl));
    }

    @Test
    void checkUpdates_jsonParsingError() {
        // arrange
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiPath = "/repos/testuser/testrepo";
        String invalidResponseBody = "{\"updated_at\":\"invalid-date\"}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(invalidResponseBody)));

        // act & assert
        assertThrows(GitHubApiException.class, () -> gitHubClient.checkUpdates(githubUrl));
    }
}
