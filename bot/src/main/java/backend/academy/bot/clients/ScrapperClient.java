package backend.academy.bot.clients;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.ApiErrorResponse;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinkResponse;
import backend.academy.bot.model.RegisterChatRequest;
import backend.academy.bot.model.RemoveLinkRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperClient {
    private static final String TG_CHAT = "/tg-chat";
    private static final String LINK = "/links";

    private final RestClient restClient;


    private <T, E> E makeAndSendRequest(
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
            return requestSpec.retrieve()
                .body(responseType);
        } catch (HttpClientErrorException ex) {
            log.error("Client error: {}", ex.getMessage());
            throw ApiErrorResponse.builder()
                .description("Ошибка запроса к сервису Scrapper: " + ex.getMessage())
                .code(String.valueOf(ex.getStatusCode().value()))
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .build();
        } catch (Exception ex) {
            log.error("Unexpected error: {}", ex.getMessage());
            throw ApiErrorResponse.builder()
                .description("Непредвиденная ошибка при запросе к сервису Scrapper: " + ex.getMessage())
                .exceptionName(ex.getClass().getSimpleName())
                .exceptionMessage(ex.getMessage())
                .build();
        }
    }

    public String registerChat(final RegisterChatRequest chat) {
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

    public String deleteChat(final Long chatId) {
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

    public ListLinkResponse getAllTrackedLinks(final Long chatId) {
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

    public LinkResponse addTrackedLink(final Long chatId, AddLinkRequest request) {
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

    public LinkResponse removeTrackedLink(Long chatId, RemoveLinkRequest request) {
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
//    private static final String TG_CHAT = "/tg-chat";
//    private static final String LINK = "/links";
//
//    private final WebClient webClient;
//
////    public Mono<Object> registerChat(final RegisterChatRequest chat){
////        log.info("Request: register chat {}", chat);
////        return webClient.post()
////            .uri("/tg-chat/{id}", chat.id())
////            .bodyValue(chat)
////            .exchangeToMono(response -> {
////                if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
////                    return response.bodyToMono(ApiErrorResponse.class);
////                }else{
////                    return response.bodyToMono(String.class);
////                }
////                }
////            );
//
////    }
//
////    public Mono<Void> deleteChat(final Long chatId){
////        log.info("Request: delete chat {}", chatId);
////        return webClient.delete()
////            .uri("tg-chat/{id}", chatId)
////            .retrieve()
////            .bodyToMono(Void.class);
////    }
//
//
////    public Mono<ListLinkResponse> getAllTrackedLinks(final Long chatId){
////        log.info("Request: get all tracked links");
////        return webClient.get()
////            .uri("/links")
////            .header("Tg-Chat-Id", String.valueOf(chatId))
////            .retrieve()
////            .bodyToMono(ListLinkResponse.class);
////    }
////
////    public Mono<ListLinkResponse> addTrackedLink(final Long chatId, AddLinkRequest request){
////        log.info("Request: add tracking link {}", request);
////        return webClient.post()
////            .uri("/links")
////            .header("Tg-Chat-Id", String.valueOf(chatId))
////            .bodyValue(request)
////            .retrieve()
////            .bodyToMono(ListLinkResponse.class);
////    }
////
////    public Mono<ListLinkResponse> removeTrackedLink(Long chatId, RemoveLinkRequest request){
////        log.info("Request: remove tracking link {}", request);
////        return webClient.method(HttpMethod.DELETE)
////            .uri("/links")
////            .header("Tg-Chat-Id")
////            .body(BodyInserters.fromValue(request))
////            .retrieve()
////            .bodyToMono(ListLinkResponse.class);
////    }
//
//    /**
//     * Универсальный метод для отправки запросов.
//     *
//     * @param uri           URI запроса
//     * @param httpMethod    HTTP метод (GET, POST, DELETE и т.д.)
//     * @param headers       Заголовки запроса
//     * @param body          Тело запроса (может быть null)
//     * @param responseType  Класс, представляющий тип ожидаемого ответа
//     * @param uriParameters Параметры URI (например, path variables)
//     * @param <T>           Тип тела запроса
//     * @param <E>           Тип ответа
//     * @return Ответ указанного типа или ошибка ApiErrorResponse
//     */
//    private <T, E> Mono<E> makeAndSendRequest(
//        String uri,
//        HttpMethod httpMethod,
//        Map<String, String> headers,
//        T body,
//        Class<E> responseType,
//        Object... uriParameters
//    ) {
//        log.info("Request: {} {}, headers: {}, body: {}", httpMethod, uri, headers, body);
//
//        WebClient.RequestBodySpec requestSpec = webClient.method(httpMethod)
//            .uri(uri, uriParameters)
//            .headers(httpHeaders -> headers.forEach(httpHeaders::add))
//            .retrieve()
//            .toEntity()
//
//        if (body != null) {
//            requestSpec.bodyValue(body);
//        }
//
//        return requestSpec.exchangeToMono(response -> {
//            if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
//                return response.bodyToMono(ApiErrorResponse.class)
//                    .flatMap(error -> Mono.error(ApiErrorResponse.builder()
//                        .description())
//                        .code(response.statusCode())
//                        .exceptionName()
//                        .exceptionMessage()
//                        .stacktrace()
//                        .build());
//            } else {
//                return response.bodyToMono(responseType);
////                return response
//            }
//        });
//    }
//
//    public Mono<String> registerChat(final RegisterChatRequest chat) {
//        Map<String, String> headers = new HashMap<>();
//        return makeAndSendRequest(
//            TG_CHAT + "/{id}",
//            HttpMethod.POST,
//            headers,
//            chat,
//            String.class,
//            chat.id()
//        );
//    }
//
//    public Mono<String> deleteChat(final Long chatId) {
//        Map<String, String> headers = new HashMap<>();
//        return makeAndSendRequest(
//            TG_CHAT + "/{id}",
//            HttpMethod.DELETE,
//            headers,
//            null,
//            String.class,
//            chatId
//        );
//    }
//
//    public Mono<ListLinkResponse> getAllTrackedLinks(final Long chatId) {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Tg-Chat-Id", String.valueOf(chatId));
//        return makeAndSendRequest(
//            LINK,
//            HttpMethod.GET,
//            headers,
//            null,
//            ListLinkResponse.class
//        );
//    }
//
//    // Пример использования метода для добавления отслеживаемой ссылки
//    public Mono<LinkResponse> addTrackedLink(final Long chatId, AddLinkRequest request) {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Tg-Chat-Id", String.valueOf(chatId));
//        return makeAndSendRequest(
//            LINK,
//            HttpMethod.POST,
//            headers,
//            request,
//            LinkResponse.class
//        );
//    }
//
//    // Пример использования метода для удаления отслеживаемой ссылки
//    public Mono<LinkResponse> removeTrackedLink(Long chatId, RemoveLinkRequest request) {
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Tg-Chat-Id", String.valueOf(chatId));
//        return makeAndSendRequest(
//            LINK,
//            HttpMethod.DELETE,
//            headers,
//            request,
//            LinkResponse.class
//        );
//    }
//
}
