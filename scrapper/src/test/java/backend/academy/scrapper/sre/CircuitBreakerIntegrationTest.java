package backend.academy.scrapper.sre;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.client.StackOverflowClient;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
            "client.stackoverflow.url=http://localhost:9090"
        })
public class CircuitBreakerIntegrationTest {

    private final String stackoverflowURL =
            "https://stackoverflow.com/questions/60200966/docker-compose-gives-invalid-environment-type-error";

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

    @DynamicPropertySource
    static void registerDynamicProps(DynamicPropertyRegistry registry) {
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

    @BeforeAll
    static void setupWireMock() {
        wireMockServer =
                new WireMockServer(WireMockConfiguration.options().port(9090).notifier(new Slf4jNotifier(true)));
        wireMockServer.start();
        configureFor("localhost", 9090);
    }

    @AfterAll
    static void tearDownWireMock() {
        if (wireMockServer != null) wireMockServer.stop();
    }

    @BeforeEach
    void resetMocks() {
        wireMockServer.resetAll();
    }

    @Test
    void stackOverflowClient_shouldOpenCircuitBreaker_OnFastFailure() throws JsonProcessingException {
        String expectedApiUrl =
                "http://localhost:9090/2.3/questions/60200966?order=desc&sort=activity&site=ru.stackoverflow";

        when(converterApi.convertStackOverflowUrlToApi(stackoverflowURL)).thenReturn(expectedApiUrl);
        when(converterApi.isStackOverflowUrl(anyString())).thenReturn(true);

        stubFor(get(urlEqualTo("/2.3/questions/60200966?order=desc&sort=activity&site=ru.stackoverflow"))
                .willReturn(aResponse()
                        .withFixedDelay(5000) // задержка больше таймаута, чтобы триггерить timeout
                        .withStatus(200)
                        .withBody("{\"items\":[]}")));

        Optional<UpdateInfo> result = stackOverflowClient.checkUpdates(stackoverflowURL);

        assertThat(result).isEmpty();

        // Повторный вызов должен пройти fallback, т.к. цепочка сработала с timeout и открыла circuit breaker
        Optional<UpdateInfo> result2 = stackOverflowClient.checkUpdates(stackoverflowURL);

        assertThat(result2).isEmpty();

        // Проверим, что оба запроса ушли (один открыл circuit, второй вернул fallback)
        verify(2, getRequestedFor(urlPathEqualTo("/2.3/questions/60200966")));
    }
}
