package backend.academy.scrapper.sre;

import dto.RegisterChatRequest;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
@TestPropertySource(
        properties = {
            "server.port=8081",
            "rate-limiting.capacity=5",
            "rate-limiting.duration=1s",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.hibernate.ddl-auto=create"
        })
public class RateLimitingIntegrationTest {
    private static final String BASE_URL = "http://localhost:8081";

    private WebTestClient webClient;

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @DynamicPropertySource
    static void registerDynamicProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("app.message.transport", () -> "HTTP");

        registry.add("client.resilience-scrapper.stackoverflow-client.timeout", () -> "100s");
        registry.add("client.resilience-scrapper.stackoverflow-client.wait-duration", () -> "5s");

        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.data.redis.username", () -> "aigunov");
        registry.add("spring.data.redis.password", () -> "12345");

        registry.add("client.resilience-scrapper.github-client.timeout", () -> "100s");
        registry.add("client.resilience-scrapper.github-client.wait-duration", () -> "5s");

        registry.add("app.scrapper.page-size", () -> 10);
        registry.add("app.scrapper.threads-count", () -> 1);
        registry.add("app.scrapper.scheduled-time", () -> 100000);
    }

    @BeforeEach
    void setUp() {
        this.webClient = WebTestClient.bindToServer()
                .baseUrl(BASE_URL)
                .responseTimeout(Duration.ofMinutes(5))
                .defaultHeader("X-Forwarded-For", "1.2.3.4")
                .build();
    }

    @Test
    void shouldReturn429TooManyRequests_WhenRateLimitExceeded() {
        String testUrl = "/tg-chat/123";

        //Arrange
        RegisterChatRequest request =
                RegisterChatRequest.builder().chatId(123L).name("test-chat").build();

        for (int i = 0; i < 5; i++) {
            //Act
            var response = webClient.post().uri(testUrl).bodyValue(request).exchange();
            log.info(response.expectBody().toString());
        }

        //Assert
        webClient
                .post()
                .uri(testUrl)
                .bodyValue(request)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
