package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.BotServiceException;
import dto.Digest;
import dto.LinkUpdate;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.message.transport", havingValue = "HTTP", matchIfMissing = true)
public class RestNotificationClient implements NotificationClient {

    private final WebClient webClient;
    @Qualifier("botRetry") private final Retry botRetry;
    @Qualifier("botTimeLimiter") private final TimeLimiter botTimeLimiter;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.message.kafka.topic.digest}")
    private String digestTopic;

    private KafkaNotificationClient fallbackClient = null;

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending update: {}", linkUpdate);
        try {
            sendRequest("/updates", linkUpdate);
        } catch (Exception e) {
            fallback(linkUpdate);
        }
    }

    @Override
    public void sendDigest(Digest digest) {
        log.info("Sending digest: chatId={} size={}", digest.tgId(), digest.updates().size());
        try {
            sendRequest("/updates/digest", digest);
        } catch (Exception e) {
            fallback(digest);
        }
    }

    private <T> void sendRequest(String uri, T body) {
        Supplier<Void> decorated = Decorators.ofSupplier(() -> {
                try {
                    return botTimeLimiter.executeFutureSupplier(() ->
                        webClient.post()
                            .uri(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body)
                            .retrieve()
                            .toBodilessEntity()
                            .doOnSuccess(res -> log.info("Success: {}", uri))
                            .doOnError(WebClientResponseException.class, e -> throwError(uri, e))
                            .then()
                            .toFuture()
                    );
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            })
            .withRetry(botRetry)
            .withFallback(List.of(Throwable.class), t -> {
                log.error("HTTP fallback for {}: {}", uri, t.getMessage());
                throw new BotServiceException("Failed after retries", t);
            })
            .decorate();

        decorated.get();
    }

    private void throwError(String uri, WebClientResponseException e) {
        log.error("HTTP error {} for {}: {}", e.getStatusCode(), uri, e.getMessage());
        throw new BotServiceException("Failed to send request", e);
    }

    private void fallback(Object message) {
        log.warn("Falling back to Kafka...");
        fallbackClient = new KafkaNotificationClient(kafkaTemplate, webClient, botRetry, botTimeLimiter);
        if (message instanceof LinkUpdate lu) fallbackClient.sendLinkUpdate(lu);
        if (message instanceof Digest d) fallbackClient.sendDigest(d);
        fallbackClient = null;
    }
}
