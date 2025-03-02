package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dto.LinkUpdate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

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

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("bot.api.url", () -> "http://localhost:8089");
    }

    @Test
    void sendLinkUpdate_success() throws Exception {
        LinkUpdate linkUpdate =
                new LinkUpdate(UUID.randomUUID(), "https://example.com/test", "Link Updated", List.of(123L, 456L));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/updates"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(linkUpdate)))
                .willReturn(WireMock.aResponse().withStatus(200)));

        notificationService.sendLinkUpdate(linkUpdate);

        wireMockServer.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/updates")));
    }

    @Test
    void sendLinkUpdate_internalServerError() throws Exception {
        LinkUpdate linkUpdate =
                new LinkUpdate(UUID.randomUUID(), "https://example.com/test", "Link Updated", List.of(123L, 456L));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/updates"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(linkUpdate)))
                .willReturn(WireMock.aResponse().withStatus(500)));

        assertThrows(BotServiceInternalErrorException.class, () -> notificationService.sendLinkUpdate(linkUpdate));
    }

    @Test
    void sendLinkUpdate_otherError() throws Exception {
        LinkUpdate linkUpdate =
                new LinkUpdate(UUID.randomUUID(), "https://example.com/test", "Link Updated", List.of(123L, 456L));

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/updates"))
                .withRequestBody(WireMock.equalToJson(objectMapper.writeValueAsString(linkUpdate)))
                .willReturn(WireMock.aResponse().withStatus(400)));

        assertThrows(BotServiceException.class, () -> notificationService.sendLinkUpdate(linkUpdate));
    }

    @Test
    void sendLinkUpdate_connectionError() throws Exception {
        LinkUpdate linkUpdate =
                new LinkUpdate(UUID.randomUUID(), "https://example.com/test", "Link Updated", List.of(123L, 456L));

        wireMockServer.stop();

        assertThrows(BotServiceException.class, () -> notificationService.sendLinkUpdate(linkUpdate));
    }
}
