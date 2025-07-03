package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest
@EnableConfigurationProperties({GitHubConfig.class})
@TestPropertySource(properties = {"app.github.url=http://localhost:8089/repos", "app.github.token=test-token"})
class GitHubClientTest {

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @Autowired
    private GitHubClient gitHubClient;

    @MockitoBean
    private LinkToApiRequestConverter converterApi;

    private WireMockServer wireMockServer;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        postgresContainer.start();
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("app.scrapper.page-size", () -> 10);
        registry.add("app.scrapper.threads-count", () -> 1);
        registry.add("app.scrapper.scheduled-time", () -> 100000);
        registry.add("app.db.access-type", () -> "orm");
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.data.redis.username", () -> "aigunov");
        registry.add("spring.data.redis.password", () -> "12345");
    }

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void checkUpdates_ShouldReturnLatestUpdateInfoFromGitHub() throws JsonProcessingException, JsonProcessingException {
        String repoPath = "/aigunov/java-shareit";
        String fullUrl = "https://github.com/aigunov/java-shareit";
        String apiUrl = "http://localhost:8089/repos" + repoPath;

        // Arrange
        when(converterApi.convertGithubUrlToApi(fullUrl)).thenReturn(apiUrl);
        when(converterApi.isGithubUrl(anyString())).thenReturn(true);

        String issuesJson =
                """
            [
              {
                "title": "Issue #1",
                "created_at": "2024-04-01T10:00:00",
                "user": { "login": "issue-author" },
                "body": "Issue body"
              }
            ]
        """;

        String prsJson =
                """
            [
              {
                "title": "PR #1",
                "created_at": "2024-04-02T12:00:00",
                "user": { "login": "pr-author" },
                "body": "Pull request body"
              }
            ]
        """;

        stubFor(get(urlEqualTo("/repos" + repoPath + "/issues?state=all"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(issuesJson)));

        stubFor(get(urlEqualTo("/repos" + repoPath + "/pulls?state=all"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(prsJson)));

        // Act
        Optional<UpdateInfo> updateInfoOpt = gitHubClient.checkUpdates(fullUrl);

        // Assert
        assertThat(updateInfoOpt).isPresent();
        UpdateInfo updateInfo = updateInfoOpt.get();

        assertThat(updateInfo.title()).isEqualTo("PR #1");
        assertThat(updateInfo.username()).isEqualTo("pr-author");
        assertThat(updateInfo.date()).isEqualTo(LocalDateTime.of(2024, 4, 2, 12, 0));
        assertThat(updateInfo.type()).isEqualTo("pull-request");
        assertThat(updateInfo.preview()).isEqualTo("Pull request body");
    }
}
