package backend.academy.scrapper.client;

import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.RestClient;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "app.github.url=http://localhost:8089/repos",
    "app.github.token=test-token"
})
class GitHubClientTest {

    @Autowired
    @Qualifier("gitHubClient")
    private GitHubClient gitHubClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("trackClient")
    private RestClient restClient;

    @Autowired
    private LinkToApiRequestConverter converterApi;

    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor(8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void checkUpdates_shouldReturnUpdatedAt() throws Exception {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);
        LocalDateTime expectedUpdatedAt = LocalDateTime.of(2023, 10, 27, 10, 0, 0);
        String expectedUpdatedAtStr = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(expectedUpdatedAt.atOffset(ZoneOffset.UTC));

        String responseBody = "{\"updated_at\":\"" + expectedUpdatedAtStr + "\"}";

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody)));

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isPresent();
        assertThat(actualUpdatedAt.get()).isEqualTo(expectedUpdatedAt);
    }

    @Test
    void checkUpdates_shouldReturnEmptyOptionalOnParsingError() {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("invalid json")));

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isEmpty();
    }

    @Test
    void checkUpdates_shouldReturnEmptyOptionalOnNotFound() {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.NOT_FOUND.value())));

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isEmpty();
    }

    @Test
    void checkUpdates_shouldReturnEmptyOptionalOnInternalServerError() {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isEmpty();
    }

    @Test
    void checkUpdates_shouldReturnEmptyOptionalOnRestClientException() {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);

        wireMockServer.stop(); // Останавливаем сервер, чтобы имитировать RestClientException

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isEmpty();

        wireMockServer.start(); // Запускаем сервер снова для остальных тестов
    }

    @Test
    void checkUpdates_shouldReturnEmptyOptionalOnNullUpdated_at() throws Exception {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);

        String responseBody = "{\"updated_at\": null}";

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody)));

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isEmpty();
    }

    @Test
    void checkUpdates_shouldReturnEmptyOptionalOnMissingUpdated_at() throws Exception {
        String githubUrl = "https://github.com/testuser/testrepo";
        String apiUrl = converterApi.convertGithubUrlToApi(githubUrl);

        String responseBody = "{}";

        stubFor(get(urlEqualTo(apiUrl.replace("http://localhost:8089", "")))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(responseBody)));

        Optional<LocalDateTime> actualUpdatedAt = gitHubClient.checkUpdates(githubUrl);

        assertThat(actualUpdatedAt).isEmpty();
    }
}
