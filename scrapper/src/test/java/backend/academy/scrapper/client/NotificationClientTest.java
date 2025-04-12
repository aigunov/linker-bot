package backend.academy.scrapper.client;

import backend.academy.scrapper.config.WebConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dto.LinkUpdate;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;


@SpringBootTest(classes = {NotificationClient.class, WebConfig.class})
class NotificationClientTest {

    @Autowired
    private NotificationClient notificationClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));

    @BeforeAll
    static void startWireMock() {
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("app.bot.url", () -> "http://localhost:8089");
    }

    @Test
    void sendLinkUpdate_shouldSendSuccessfully() throws Exception {
        // Given
        LinkUpdate update = LinkUpdate.builder()
            .id(UUID.randomUUID())
            .url("https://github.com/test/repo")
            .message("New update")
            .tgChatIds(Set.of(123L))
            .build();

        stubFor(post(urlEqualTo("/updates"))
            .willReturn(aResponse()
                .withStatus(200)));

        // When
        notificationClient.sendLinkUpdate(update);
        Thread.sleep(200); // ensure async call finishes

        // Then
        verify(postRequestedFor(urlEqualTo("/updates"))
            .withRequestBody(equalToJson(objectMapper.writeValueAsString(update)))
            .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    void sendLinkUpdate_shouldLogErrorOnBadRequest() throws InterruptedException {
        // Given
        LinkUpdate update = LinkUpdate.builder()
            .id(UUID.randomUUID())
            .url("https://github.com/test/repo")
            .message("Invalid update")
            .tgChatIds(Set.of(123L))
            .build();

        stubFor(post(urlEqualTo("/updates"))
            .willReturn(aResponse().withStatus(400)));

        // When
        notificationClient.sendLinkUpdate(update);
        Thread.sleep(200);

        // Then
        verify(postRequestedFor(urlEqualTo("/updates")));
    }
}
