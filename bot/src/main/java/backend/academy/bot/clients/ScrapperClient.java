package backend.academy.bot.clients;

import dto.AddLinkRequest;
import dto.ApiErrorResponse;
import dto.GetAllTagsRequest;
import dto.GetLinksRequest;
import dto.GetTagsResponse;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.NotificationTimeRequest;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperClient {
    private static final String TG_CHAT = "/tg-chat";
    private static final String LINK = "/links";
    private static final String TAGS = "/tags";

    private final RestClient restClient;
    private final JsonToApiErrorResponse convertJsonToApiErrorResponse;
    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    public ResponseEntity<Object> registerChat(final RegisterChatRequest chat) {
        log.info("Request: register chat {}", chat);
        Map<String, String> headers = new HashMap<>();
        return makeAndSendRequest(TG_CHAT + "/{chatId}", HttpMethod.POST, headers, chat, String.class, chat.chatId());
    }

    public ResponseEntity<Object> deleteChat(final Long chatId) {
        log.info("Request: delete chat {}", chatId);
        Map<String, String> headers = new HashMap<>();
        return makeAndSendRequest(TG_CHAT + "/{chatId}", HttpMethod.DELETE, headers, null, String.class, chatId);
    }

    public ResponseEntity<Object> setNotificationTime(Long chatId, NotificationTimeRequest notificationTimeRequest) {
        log.info("Request: change time on [{}] for chat: {}", notificationTimeRequest.time(), chatId);
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(
            TG_CHAT + "/time/{chatId}", HttpMethod.POST, headers, notificationTimeRequest, String.class, chatId);
    }

    public ResponseEntity<Object> getAllLinks(final Long chatId, final GetLinksRequest linksRequest) {
        log.info("Request: get all tracked links");
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(LINK + "/getLinks", HttpMethod.POST, headers, linksRequest, ListLinkResponse.class);
    }

    public ResponseEntity<Object> getAllTags(long chatId) {
        log.info("Request: get all tags from chat: {}", chatId);
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(TAGS, HttpMethod.GET, headers, new GetAllTagsRequest(), GetTagsResponse.class);
    }

    public ResponseEntity<Object> addTrackedLink(final Long chatId, AddLinkRequest request) {
        log.info("Request: add tracking link {}", request);
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(LINK, HttpMethod.POST, headers, request, LinkResponse.class);
    }

    public ResponseEntity<Object> removeTrackedLink(Long chatId, RemoveLinkRequest request) {
        log.info("Request: remove tracking link {}", request);
        Map<String, String> headers = new HashMap<>();
        headers.put("Tg-Chat-Id", String.valueOf(chatId));
        return makeAndSendRequest(LINK, HttpMethod.DELETE, headers, request, LinkResponse.class);
    }

    private <T, E> ResponseEntity<Object> makeAndSendRequest(
        String uri,
        HttpMethod httpMethod,
        Map<String, String> headers,
        T body,
        Class<E> responseType,
        Object... uriParameters) {

        log.info("Request: {} {}, headers: {}, body: {}", httpMethod, uri, headers, body);

        RestClient.RequestHeadersSpec<?> requestSpec = restClient
            .method(httpMethod)
            .uri(uri, uriParameters)
            .body(body != null ? body : new Object())
            .header(HttpHeaders.CONTENT_TYPE, String.valueOf(MediaType.APPLICATION_JSON))
            .headers(httpHeaders -> headers.forEach(httpHeaders::add));

        Retry retry = retryRegistry.retry("scrapperClient");
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("scrapperClient");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("scrapperClient");

        Supplier<CompletableFuture<ResponseEntity<Object>>> supplier = () -> CompletableFuture.supplyAsync(() -> {
            try {
                ResponseEntity<E> response = requestSpec.retrieve().toEntity(responseType);
                log.info("Response: {} {}", response.getStatusCode(), response.getBody());
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            } catch (RestClientResponseException ex) {
                log.error("Client error: {}", ex.getMessage());
                return handleRestClientResponseException(ex);
            } catch (RestClientException ex) {
                log.error("Unexpected error: {}", ex.getMessage());
                return handleRestClientException(ex);
            }
        });

        Supplier<ResponseEntity<Object>> decoratedSupplier = Decorators.ofSupplier(() -> {
                try {
                    return timeLimiter.executeFutureSupplier(supplier);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            })
            .withRetry(retry)
            .withCircuitBreaker(circuitBreaker)
            .withFallback(List.of(Throwable.class), t -> {
                log.warn("Fallback executed due to: {}", t.getMessage());
                return createDefaultErrorResponse(
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    t.getMessage(),
                    t.getClass().getSimpleName(),
                    convertStackTraceToList(t.getStackTrace()));
            })
            .decorate();

        return decoratedSupplier.get();
    }

    private ResponseEntity<Object> handleRestClientResponseException(RestClientResponseException ex) {
        try {
            String responseBody = ex.getResponseBodyAsString();
            ApiErrorResponse errorResponse = convertJsonToApiErrorResponse.convertJsonToApiErrorResponse(responseBody);
            log.error("Ошибка от Scrapper: {}", errorResponse);
            return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        } catch (IOException parseEx) {
            log.error("Ошибка парсинга ответа: {}", parseEx.getMessage());
            return createDefaultErrorResponse(
                ex.getStatusCode().value(),
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                convertStackTraceToList(ex.getStackTrace()));
        }
    }

    private ResponseEntity<Object> handleRestClientException(RestClientException ex) {
        return createDefaultErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getMessage(),
            ex.getClass().getSimpleName(),
            convertStackTraceToList(ex.getStackTrace()));
    }

    private ResponseEntity<Object> createDefaultErrorResponse(
        int statusCode, String message, String exceptionName, List<String> stacktrace) {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .description("Ошибка запроса к сервису Scrapper: " + message)
            .code(String.valueOf(statusCode))
            .exceptionName(exceptionName)
            .exceptionMessage(message)
            .stacktrace(stacktrace)
            .build();
        return ResponseEntity.status(statusCode).body(errorResponse);
    }

    public static List<String> convertStackTraceToList(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace).map(StackTraceElement::toString).collect(Collectors.toList());
    }
}
