package backend.academy.scrapper.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.exception.StackOverflowApiException;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@SpringBootTest
@EnableConfigurationProperties({GitHubConfig.class, StackOverflowConfig.class})
@TestPropertySource(
        properties = {
            "app.github.url=http://localhost:8089/repos",
            "app.github.token=test-token",
            "app.stackoverflow.key=test-key",
            "app.stackoverflow.access_token=test-access",
            "app.stackoverflow.url=http://localhost:8089/stackoverflow"
        })
class StackOverflowClientTest {
    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @Autowired
    private StackOverflowClient stackOverflowClient;

    @Autowired
    private LinkToApiRequestConverter converterApi;

    @Autowired
    private ObjectMapper objectMapper;

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
    void checkUpdates_ShouldReturnLatestAnswer_WhenAnswersExist() throws JsonProcessingException {
        // Given
        String questionUrl = "https://stackoverflow.com/questions/12345678/some-question";
        String apiUrl = "/stackoverflow/12345678?order=desc&sort=activity&site=ru.stackoverflow";

        String responseJson =
                """
            {
                "items": [
                    {
                        "title": "Как настроить Spring Boot?",
                        "answers": [
                            {
                                "creation_date": 1712345678,
                                "body": "Используйте @SpringBootApplication...",
                                "owner": { "display_name": "user1" }
                            }
                        ],
                        "comments": [
                            {
                                "creation_date": 1712345600,
                                "body": "Попробуйте добавить зависимость...",
                                "owner": { "display_name": "user2" }
                            }
                        ]
                    }
                ]
            }
            """;

        stubFor(get(urlEqualTo(apiUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));

        // When
        Optional<UpdateInfo> updateInfo = stackOverflowClient.checkUpdates(questionUrl);

        // Then
        assertThat(updateInfo).isPresent();
        assertThat(updateInfo.get().type()).isEqualTo("answer");
        assertThat(updateInfo.get().username()).isEqualTo("user1");
        assertThat(updateInfo.get().date()).isEqualTo(LocalDateTime.ofEpochSecond(1712345678, 0, ZoneOffset.UTC));
    }

//    @Test
//    void checkUpdates_ShouldReturnLatestComment_WhenNoAnswersButCommentsExist() {
//        // Given
//        String questionUrl = "https://stackoverflow.com/questions/12345678/some-question";
//        String apiUrl = "/stackoverflow/12345678?order=desc&sort=activity&site=ru.stackoverflow";
//
//        String responseJson =
//                """
//            {
//                "items": [
//                    {
//                        "title": "Как настроить Spring Boot?",
//                        "answers": [],
//                        "comments": [
//                            {
//                                "creation_date": 1712345600,
//                                "body": "Попробуйте добавить зависимость...",
//                                "owner": { "display_name": "user2" }
//                            }
//                        ]
//                    }
//                ]
//            }
//            """;
//
//        stubFor(get(urlEqualTo(apiUrl))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(responseJson)));
//
//        // When
//        Optional<UpdateInfo> updateInfo = stackOverflowClient.checkUpdates(questionUrl);
//
//        // Then
//        assertThat(updateInfo).isPresent();
//        assertThat(updateInfo.get().type()).isEqualTo("comment");
//    }

//    @Test
//    void checkUpdates_ShouldReturnEmpty_WhenNoUpdates() {
//        // Given
//        String questionUrl = "https://stackoverflow.com/questions/12345678/some-question";
//        String apiUrl = "/stackoverflow/12345678?order=desc&sort=activity&site=ru.stackoverflow";
//
//        String responseJson = """
//            { "items": [] }
//            """;
//
//        stubFor(get(urlEqualTo(apiUrl))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json")
//                        .withBody(responseJson)));
//
//        // When
//        Optional<UpdateInfo> updateInfo = stackOverflowClient.checkUpdates(questionUrl);
//
//        // Then
//        assertThat(updateInfo).isEmpty();
//    }

    @Test
    void checkUpdates_ShouldThrow_WhenInvalidJsonResponse() {
        // Given
        String questionUrl = "https://stackoverflow.com/questions/12345678/some-question";
        String apiUrl = "/stackoverflow/12345678?order=desc&sort=activity&site=ru.stackoverflow";

        stubFor(get(urlEqualTo(apiUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ invalid json }")));

        // When / Then
        assertThatThrownBy(() -> stackOverflowClient.checkUpdates(questionUrl))
                .isInstanceOf(StackOverflowApiException.class)
                .hasMessageContaining("Ошибка при обработке JSON-ответа");
    }
}
