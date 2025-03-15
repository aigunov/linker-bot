package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.StackOverflowApiException;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@TestPropertySource(properties = {"app.stackoverflow.url=http://localhost:8089/questions"})
class StackOverflowClientTest {

    @Autowired
    @Qualifier("stackOverflowClient")
    private StackOverflowClient stackOverflowClient;

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
    void checkUpdates_success() {
        // arrange
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";
        long lastActivityDate = 1700000000L;
        String responseBody = "{\"items\": [{\"last_activity_date\": " + lastActivityDate + "}]}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        // act
        Optional<LocalDateTime> result = stackOverflowClient.checkUpdates(stackOverflowUrl);

        // assert
        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.ofEpochSecond(lastActivityDate, 0, ZoneOffset.UTC), result.get());
    }

    @Test
    void checkUpdates_clientError() {
        // arrange
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse().withStatus(404)));

        // act & assert
        assertThrows(StackOverflowApiException.class, () -> stackOverflowClient.checkUpdates(stackOverflowUrl));
    }

    @Test
    void checkUpdates_serverError() {
        // arrange
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse().withStatus(500)));

        // act & assert
        assertThrows(StackOverflowApiException.class, () -> stackOverflowClient.checkUpdates(stackOverflowUrl));
    }

    @Test
    void checkUpdates_jsonParsingError() {
        // arrange
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";
        String invalidResponseBody = "{\"items\": [{\"last_activity_date\": \"invalid-date\"}]}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(invalidResponseBody)));

        // act & assert
        assertThrows(StackOverflowApiException.class, () -> stackOverflowClient.checkUpdates(stackOverflowUrl));
    }

    @Test
    void checkUpdates_emptyItems() {
        // arrange
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";
        String emptyItemsResponseBody = "{\"items\": []}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(emptyItemsResponseBody)));

        // act
        Optional<LocalDateTime> result = stackOverflowClient.checkUpdates(stackOverflowUrl);

        // assert
        assertFalse(result.isPresent());
    }
}
