package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import dto.Digest;
import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.message.transport", havingValue = "HTTP")
public class RestNotificationClient implements NotificationClient {

    private final WebClient webClient;

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending single update: {}", linkUpdate);
        sendRequest("/updates", linkUpdate);
    }

    @Override
    public void sendDigest(Digest digest) {
        log.info("Sending digest with {} updates for chatId={}", digest, digest.tgId());
        sendRequest("/updates/digest/", digest);
    }

    private <T> void sendRequest(String uri, T body) {
        webClient
            .post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .toBodilessEntity()
            .doOnSuccess(res -> log.info("Successfully sent request to {}", uri))
            .doOnError(WebClientResponseException.class, e -> {
                if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                    log.error("Bot service returned 500 for request to {}: {}", uri, e.getMessage());
                    throw new BotServiceInternalErrorException("Internal error from bot service", e);
                } else {
                    log.error("Failed to send request to {}: {}", uri, e.getMessage());
                    throw new BotServiceException("Failed to send request", e);
                }
            })
            .doOnError(Exception.class, e -> {
                log.error("Error during request to {}: {}", uri, e.getMessage());
                throw new BotServiceException("Failed to send request", e);
            })
            .subscribe();
    }
}
