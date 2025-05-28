package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.StackOverflowResponse;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class StackOverflowClient extends AbstractUpdateCheckingClient {

    public StackOverflowClient(RestClient restClient, LinkToApiRequestConverter converterApi) {
        super(restClient, converterApi);
    }

    @Override
    @Retry(name = "stackOverflowClient", fallbackMethod = "fallback")
    @TimeLimiter(name = "stackOverflowClient")
    @CircuitBreaker(name = "stackOverflowClient", fallbackMethod = "fallback")
    public Optional<UpdateInfo> checkUpdates(String link) throws JsonProcessingException {
        String apiUrl = converterApi.convertStackOverflowUrlToApi(link);
        log.info("Checking for StackOverflow updates... {}", apiUrl);

        try {
            StackOverflowResponse response = fetchResponse(apiUrl);
            return determineLatestUpdate(response);
        } catch (RestClientException e) {
            log.error("RestClientException while accessing StackOverflow API for link {}: {}", link, e.getMessage());
            throw e;
        }
    }

    public Optional<UpdateInfo> fallback(String link, Throwable t) {
        log.warn("StackOverflowClient fallback executed for {} due to: {}", link, t.toString());
        return Optional.empty();
    }

    private StackOverflowResponse fetchResponse(String apiUrl) throws JsonProcessingException {
        var response = restClient.get()
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

        Optional<UpdateInfo> latestAnswer = item.answers() != null && !item.answers().isEmpty()
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

        Optional<UpdateInfo> latestComment = item.comments() != null && !item.comments().isEmpty()
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
