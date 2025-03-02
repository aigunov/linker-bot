package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.StackOverflowApiException;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = {
    "app.stackoverflow.url=http://localhost:8089/questions"
})
class StackOverflowClientTest {

    @Autowired
    @Qualifier("stackOverflowClient")
    private StackOverflowClient stackOverflowClient;

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
    void checkUpdates_success() {
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";
        long lastActivityDate = 1700000000L;
        String responseBody = "{\"items\": [{\"last_activity_date\": " + lastActivityDate + "}]}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        Optional<LocalDateTime> result = stackOverflowClient.checkUpdates(stackOverflowUrl);

        assertTrue(result.isPresent());
        assertEquals(LocalDateTime.ofEpochSecond(lastActivityDate, 0, ZoneOffset.UTC), result.get());
    }

    @Test
    void checkUpdates_clientError() {
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(404)));

        assertThrows(StackOverflowApiException.class, () -> stackOverflowClient.checkUpdates(stackOverflowUrl));
    }

    @Test
    void checkUpdates_serverError() {
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(500)));

        assertThrows(StackOverflowApiException.class, () -> stackOverflowClient.checkUpdates(stackOverflowUrl));
    }

    @Test
    void checkUpdates_jsonParsingError() {
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";
        String invalidResponseBody = "{\"items\": [{\"last_activity_date\": \"invalid-date\"}]}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(invalidResponseBody)));

        assertThrows(StackOverflowApiException.class, () -> stackOverflowClient.checkUpdates(stackOverflowUrl));
    }

    @Test
    void checkUpdates_emptyItems() {
        String stackOverflowUrl = "https://stackoverflow.com/questions/12345678/test-question";
        String apiPath = "/questions/12345678?order=desc&sort=activity&site=ru.stackoverflow";
        String emptyItemsResponseBody = "{\"items\": []}";

        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(apiPath))
            .willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(emptyItemsResponseBody)));

        Optional<LocalDateTime> result = stackOverflowClient.checkUpdates(stackOverflowUrl);
        assertFalse(result.isPresent());
    }
}
