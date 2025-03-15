package backend.academy.bot.clients;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dto.AddLinkRequest;
import dto.ApiErrorResponse;
import dto.RegisterChatRequest;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"scrapper.api.url=http://localhost:8089"})
class ScrapperClientTest {

    @Autowired
    private ScrapperClient scrapperClient;

    @Autowired
    private ObjectMapper objectMapper;

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
    void registerChat_shouldReturnOk() throws Exception {
        // arrange
        RegisterChatRequest registerChatRequest =
            RegisterChatRequest.builder().chatId(123L).name("test").build();
        String requestBody = objectMapper.writeValueAsString(registerChatRequest);

        stubFor(post(urlEqualTo("/tg-chat/123"))
            .withRequestBody(equalToJson(requestBody))
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("Registered")));

        // act
        ResponseEntity<Object> response = scrapperClient.registerChat(registerChatRequest);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Registered");
    }

    @Test
    void registerChat_ShouldReturnSuccess() {
        // arrange
        RegisterChatRequest request = new RegisterChatRequest(1L, "Test Chat");

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/tg-chat/1"))
            .willReturn(WireMock.aResponse().withStatus(200)));

        // act
        ResponseEntity<Object> response = scrapperClient.registerChat(request);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addTrackedLink_ShouldReturnLinkResponse() {
        // arrange
        AddLinkRequest request = AddLinkRequest.builder()
            .uri("http://example.com")
            .tags(List.of())
            .filters(List.of())
            .build();

        wireMockServer.stubFor(
            WireMock.post(WireMock.urlEqualTo("/links"))
                .withHeader("Tg-Chat-Id", equalTo("1"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            "{\"id\": \"550e8400-e29b-41d4-a716-446655440000\", \"url\": \"http://example.com\", \"tags\":[], \"filters\":[]}")));

        // act
        ResponseEntity<Object> response = scrapperClient.addTrackedLink(1L, request);

        // assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void registerChat_shouldHandleError() {
        // arrange
        RegisterChatRequest request =
            RegisterChatRequest.builder().chatId(123L).name("test").build();
        stubFor(post(urlEqualTo("/tg-chat/123"))
            .willReturn(aResponse()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"message\":\"Bad Request\",\"status\":\"400\"}")));

        // act
        ResponseEntity<Object> response = scrapperClient.registerChat(request);

        // assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ApiErrorResponse.class);
    }
}
