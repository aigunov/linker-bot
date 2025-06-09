package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.StackOverflowResponse;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class StackOverflowClient extends AbstractUpdateCheckingClient {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    public StackOverflowClient(
            RestClient restClient,
            LinkToApiRequestConverter converterApi,
            @Qualifier("stackoverflowCircuitBreaker") CircuitBreaker stackoverflowCircuitBreaker,
            @Qualifier("stackoverflowRetry") Retry stackoverflowRetry,
            @Qualifier("stackoverflowTimeLimiter") TimeLimiter stackoverflowTimeLimiter) {
        super(restClient, converterApi);
        objectMapper.registerModule(new JavaTimeModule());

        this.circuitBreaker = stackoverflowCircuitBreaker;
        this.retry = stackoverflowRetry;
        this.timeLimiter = stackoverflowTimeLimiter;
    }

    @Override
    public Optional<UpdateInfo> checkUpdates(String link) throws JsonProcessingException {
        String apiUrl = converterApi.convertStackOverflowUrlToApi(link);
        log.info("Checking for StackOverflow updates... {}", apiUrl);

        Supplier<Optional<UpdateInfo>> decoratedSupplier = Decorators.ofSupplier(() -> {
                    try {
                        return fetchResponseWithTimeLimiter(apiUrl);
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                })
                .withRetry(retry)
                .withCircuitBreaker(circuitBreaker)
                .withFallback(List.of(Throwable.class), t -> {
                    log.warn("Fallback executed for StackOverflowClient due to: {}", t.getMessage());
                    return Optional.empty();
                })
                .decorate();

        return decoratedSupplier.get();
    }

    private Optional<UpdateInfo> fetchResponseWithTimeLimiter(String apiUrl) throws Exception {
        return timeLimiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(() -> {
            try {
                StackOverflowResponse response = fetchResponse(apiUrl);
                return determineLatestUpdate(response);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }));
    }

    private StackOverflowResponse fetchResponse(String apiUrl) throws JsonProcessingException {
        var response = restClient
                .get()
                .uri(apiUrl)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    log.error("StackOverflow API error for URL: {} (status: {})", apiUrl, res.getStatusCode());
                    throw new RestClientException("StackOverflow API error for URL: " + apiUrl);
                })
                .toEntity(String.class);
        return objectMapper.readValue(response.getBody(), StackOverflowResponse.class);
    }

    private Optional<UpdateInfo> determineLatestUpdate(StackOverflowResponse parsedResponse) {
        if (parsedResponse.items() == null || parsedResponse.items().isEmpty()) {
            return Optional.empty();
        }

        var item = parsedResponse.items().getFirst();
        var title = item.title();

        Optional<UpdateInfo> latestAnswer = item.answers() != null
                        && !item.answers().isEmpty()
                ? item.answers().stream()
                        .max(Comparator.comparingLong(StackOverflowResponse.StackOverflowItem.Answer::creationDate))
                        .map(ans -> UpdateInfo.builder()
                                .date(LocalDateTime.ofEpochSecond(ans.creationDate(), 0, ZoneOffset.UTC))
                                .title(title)
                                .username(ans.owner().displayName())
                                .type("answer")
                                .preview(StringUtils.substring(ans.body(), 0, 200))
                                .build())
                : Optional.empty();

        Optional<UpdateInfo> latestComment = item.comments() != null
                        && !item.comments().isEmpty()
                ? item.comments().stream()
                        .max(Comparator.comparingLong(StackOverflowResponse.StackOverflowItem.Comment::creationDate))
                        .map(c -> UpdateInfo.builder()
                                .date(LocalDateTime.ofEpochSecond(c.creationDate(), 0, ZoneOffset.UTC))
                                .title(title)
                                .username(c.owner().displayName())
                                .type("comment")
                                .preview(StringUtils.substring(c.body(), 0, 200))
                                .build())
                : Optional.empty();

        return Stream.of(latestAnswer, latestComment)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.comparing(UpdateInfo::date));
    }
}
