package backend.academy.scrapper.sre;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.client.GitHubClient;
import backend.academy.scrapper.client.RestNotificationClient;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

@SpringBootTest(classes = ScrapperApplication.class)
@Testcontainers
@TestPropertySource(
        properties = {
            "app.db.access-type=orm",
            "spring.jpa.hibernate.ddl-auto=create",
            "app.scrapper.page-size=10",
            "app.scrapper.threads-count=1",
            "app.scrapper.scheduled-time=100000",
            "app.digest.threads-count=4",
            "app.message.transport=HTTP",
            "app.digest.scheduler-time=60000",
            "client.stackoverflow.url=http://localhost:9090",
            "client.github.url=http://localhost:9090",
            "client.bot.url=http://localhost:9090",
        })
public class RetryIntegrationTest {

    private final String stackoverflowURL =
            "https://stackoverflow.com/questions/60200966/docker-compose-gives-invalid-environment-type-error";
    private final String githubURL = "https://github.com/central-university-dev/java-aigunov";
    private final String stackoverflowBody =
            """
        {
          "items": [
            {
              "last_activity_date": 1717000000,
              "title": "Как использовать WireMock для тестирования Resilience4j Retry?",
              "answers": [
                {
                  "creation_date": 1717000000,
                  "body": "Для тестирования Retry с WireMock, настройте сценарий",
                  "owner": {
                    "display_name": "Тестовый Пользователь 1"
                  }
                },
                {
                  "creation_date": 1717000100,
                  "body": "Важно помнить, что WireMock работает на уровне HTTP.",
                  "owner": {
                    "display_name": "Тестовый Пользователь 2"
                  }
                }
              ],
              "comments": [
                {
                  "creation_date": 1717000200,
                  "body": "Очень полезно, спасибо за ответ!",
                  "owner": {
                    "display_name": "Комментатор 1"
                  }
                }
              ]
            }
          ]
        }
        """;

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    static WireMockServer wireMockServer;

    @MockitoBean
    private LinkToApiRequestConverter converterApi;

    @Autowired
    private StackOverflowClient stackOverflowClient;

    @Autowired
    private GitHubClient gitHubClient;

    @Autowired
    private RestNotificationClient botClient;

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.message.transport", () -> "HTTP");

        registry.add("client.resilience-scrapper.stackoverflow-client.timeout", () -> "100s");
        registry.add("client.resilience-scrapper.stackoverflow-client.wait-duration", () -> "5s");

        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.data.redis.username", () -> "aigunov");
        registry.add("spring.data.redis.password", () -> "12345");

        registry.add("client.resilience-scrapper.github-client.timeout", () -> "100s");
        registry.add("client.resilience-scrapper.github-client.wait-duration", () -> "5s");
    }

    @BeforeAll
    static void setupWireMock() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.options().port(9090).notifier(new Slf4jNotifier(true)));
        wireMockServer.start();
        WireMock.configureFor("localhost", 9090);
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null) wireMockServer.stop();
    }

    @Test
    void stackOverflowClient_shouldRetryOnServerError() throws JsonProcessingException {
        String expectedStackOverflowApiUrl =
                "http://localhost:9090/2.3/questions/60200966?order=desc&sort=activity&site=ru.stackoverflow";

        // Arrange
        when(converterApi.convertStackOverflowUrlToApi(stackoverflowURL)).thenReturn(expectedStackOverflowApiUrl);
        when(converterApi.isStackOverflowUrl(anyString())).thenReturn(true);

        wireMockServer.resetAll();

        stubFor(get(urlEqualTo("/2.3/questions/60200966?order=desc&sort=activity&site=ru.stackoverflow"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("Second Attempt"));

        stubFor(get(urlEqualTo("/2.3/questions/60200966?order=desc&sort=activity&site=ru.stackoverflow"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Second Attempt")
                .willReturn(aResponse().withStatus(200).withBody(stackoverflowBody)));

        // Act
        Optional<UpdateInfo> result = stackOverflowClient.checkUpdates(stackoverflowURL);

        // Assert
        assertThat(result).isNotEmpty();

        verify(
                2,
                getRequestedFor(urlPathEqualTo("/2.3/questions/60200966"))
                        .withQueryParam("order", equalTo("desc"))
                        .withQueryParam("sort", equalTo("activity"))
                        .withQueryParam("site", equalTo("ru.stackoverflow")));
    }

    @Test
    void gitHubClient_shouldRetryOnServerError() throws JsonProcessingException {
        String expectedGitHubApiUrl = "http://localhost:9090/repos/central-university-dev/java-aigunov";
        String fullGitHubUrl = "https://github.com/central-university-dev/java-aigunov";

        // Arrange
        String issuesResponse =
                """
        [
            {
                "title": "Issue #1",
                "created_at": "2024-06-01T12:00:00",
                "user": { "login": "issue-author" },
                "body": "Issue body text"
            }
        ]
        """;

        String prsResponse =
                """
        [
            {
                "title": "PR #1",
                "created_at": "2024-06-02T15:30:00",
                "user": { "login": "pr-author" },
                "body": "Pull request body"
            }
        ]
        """;

        when(converterApi.convertGithubUrlToApi(fullGitHubUrl)).thenReturn(expectedGitHubApiUrl);
        when(converterApi.isGithubUrl(anyString())).thenReturn(true);

        wireMockServer.resetAll();

        stubFor(get(urlEqualTo("/repos/central-university-dev/java-aigunov/issues?state=all"))
                .inScenario("GitHub Retry")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo("Second Attempt"));

        stubFor(get(urlEqualTo("/repos/central-university-dev/java-aigunov/issues?state=all"))
                .inScenario("GitHub Retry")
                .whenScenarioStateIs("Second Attempt")
                .willReturn(aResponse().withStatus(200).withBody(issuesResponse)));

        stubFor(get(urlEqualTo("/repos/central-university-dev/java-aigunov/pulls?state=all"))
                .willReturn(aResponse().withStatus(200).withBody(prsResponse)));

        // Act
        Optional<UpdateInfo> result = gitHubClient.checkUpdates(fullGitHubUrl);

        // Assert
        assertThat(result).isPresent();
        UpdateInfo update = result.get();

        assertThat(update.title()).isEqualTo("PR #1");
        assertThat(update.username()).isEqualTo("pr-author");
        assertThat(update.type()).isEqualTo("pull-request");
        assertThat(update.preview()).contains("Pull request body");
        assertThat(update.date()).isEqualTo(LocalDateTime.of(2024, 6, 2, 15, 30));
    }
}
