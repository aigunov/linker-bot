package backend.academy.bot.clients;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import dto.AddLinkRequest;
import dto.ApiErrorResponse;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperClient {
    private static final String TG_CHAT = "/tg-chat";
    private static final String LINK = "/links";

    private final RestClient restClient;


    private <T, E> ResponseEntity<Object> makeAndSendRequest(
        String uri,
        HttpMethod httpMethod,
        Map<String, String> headers,
        T body,
        Class<E> responseType,
        Object... uriParameters
    ) {
        log.info("Request: {} {}, headers: {}, body: {}", httpMethod, uri, headers, body);

        RestClient.RequestHeadersSpec<?> requestSpec = restClient.method(httpMethod)
            .uri(uri, uriParameters)
            .body(body != null ? body : new Object())
            .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
            .headers(httpHeaders -> headers.forEach(httpHeaders::add));

        try {
            ResponseEntity<E> response = requestSpec.retrieve().toEntity(responseType);
            log.info("Response: {} {}", response.getStatusCode(), response.getBody());
            return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
        } catch (HttpClientErrorException ex) {
            log.error("Client error: {}", ex.getMessage());
            var errorResponse = ApiErrorResponse.builder()
                .description("Ошибка запроса к сервису Scrapper: " + ex.getMessage())
                .code(String.valueOf(ex.getStatusCode().value()))
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .stacktrace(convertStackTraceToList(ex.getStackTrace()))
                .build();
            return ResponseEntity
                .status(ex.getStatusCode())
                .body(errorResponse);
        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage());
            var errorResponse = ApiErrorResponse.builder()
                .description("Непредвиденная ошибка при запросе к сервису Scrapper: " + ex.getMessage())
                .code("500")
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .stacktrace(convertStackTraceToList(ex.getStackTrace()))
                .build();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }


    public ResponseEntity<Object> registerChat(final RegisterChatRequest chat) {
        log.info("Request: register chat {}", chat);
        Map<String, String> headers = new HashMap<>();
        return makeAndSendRequest(
            TG_CHAT + "/{id}",
            HttpMethod.POST,
            headers,
            chat,
            String.class,
            chat.id()
        );
    }

    public ResponseEntity<Object> deleteChat(final Long chatId) {
        log.info("Request: delete chat {}", chatId);
        Map<String, String> headers = new HashMap<>();
        return makeAndSendRequest(
            TG_CHAT + "/{id}",
            HttpMethod.DELETE,
            headers,
            null,
            String.class,
            chatId
        );
    }

    public ResponseEntity<Object> getAllTrackedLinks(final Long chatId) {
        log.info("Request: get all tracked links");
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(
            LINK,
            HttpMethod.GET,
            headers,
            null,
            ListLinkResponse.class
        );
    }

    public ResponseEntity<Object> addTrackedLink(final Long chatId, AddLinkRequest request) {
        log.info("Request: add tracking link {}", request);
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(
            LINK,
            HttpMethod.POST,
            headers,
            request,
            LinkResponse.class
        );
    }

    public ResponseEntity<Object> removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        log.info("Request: remove tracking link {}", request);
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(
            LINK,
            HttpMethod.DELETE,
            headers,
            request,
            LinkResponse.class
        );
    }

    public static List<String> convertStackTraceToList(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace)
            .map(StackTraceElement::toString)
            .collect(Collectors.toList());
    }
}
