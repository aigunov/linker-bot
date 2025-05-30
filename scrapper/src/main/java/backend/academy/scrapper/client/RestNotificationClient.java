package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.BotServiceException;
import dto.Digest;
import dto.LinkUpdate;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.message.transport", havingValue = "HTTP")
public class RestNotificationClient implements NotificationClient {

    private final WebClient webClient;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

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
        Retry retry = retryRegistry.retry("botClient");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("botClient");

        Callable<Void> callable = () -> webClient
            .post()
            .uri(uri)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .toBodilessEntity()
            .doOnSuccess(res -> log.info("Successfully sent request to {}", uri))
            .doOnError(WebClientResponseException.class, e -> {
                log.error("Bot service returned {} for request to {}: {}", e.getStatusCode(), uri, e.getMessage());
                throw new BotServiceException("Failed to send request", e);
            })
            .doOnError(Exception.class, e -> {
                log.error("Error during request to {}: {}", uri, e.getMessage());
                throw new BotServiceException("Failed to send request", e);
            })
            .then()
            .toFuture()
            .get(); // block and wait for result

        Callable<Void> decorated =
            TimeLimiter.decorateFutureSupplier(timeLimiter, () -> CompletableFuture.supplyAsync(() -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }));

        Callable<Void> withRetry = Retry.decorateCallable(retry, decorated);

        try {
            withRetry.call();
        } catch (Exception e) {
            log.error("Failed to send notification after retries: {}", e.getMessage(), e);
            throw new BotServiceException("All retries failed", e);
        }
    }
}
