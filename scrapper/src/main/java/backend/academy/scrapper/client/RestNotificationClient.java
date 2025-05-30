package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.BotServiceException;
import dto.Digest;
import dto.LinkUpdate;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
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
        log.info("Sending digest with {} updates for chatId={}", digest.updates().size(), digest.tgId());
        sendRequest("/updates/digest/", digest);
    }

    private <T> void sendRequest(String uri, T body) {
        Retry retry = retryRegistry.retry("botClient");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("botClient");

        Supplier<CompletableFuture<Void>> supplier = () -> webClient
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
            .toFuture();

        Supplier<Void> decorated = Decorators.ofSupplier(() -> {
                try {
                    return timeLimiter.executeFutureSupplier(supplier);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            })
            .withRetry(retry)
            .withFallback(List.of(Throwable.class), t -> {
                log.error("Failed to send notification after retries: {}", t.getMessage());
                throw new BotServiceException("All retries failed", t);
            })
            .decorate();

        try {
            decorated.get();
        } catch (Exception e) {
            // fallback уже сработал внутри, повторный throw из decorated.get()
            if (e instanceof BotServiceException bse) {
                throw bse;
            } else {
                throw new BotServiceException("Unexpected exception during sending", e);
            }
        }
    }
}
