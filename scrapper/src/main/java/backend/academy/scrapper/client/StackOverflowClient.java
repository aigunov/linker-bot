package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.StackOverflowApiException;
import backend.academy.scrapper.data.dto.StackOverflowResponse;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
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
    public Optional<LocalDateTime> checkUpdates(String link) {
        String apiUrl = converterApi.convertStackOverflowUrlToApi(link);
        log.info("Checking for updates... {}", apiUrl);

        try {
            ResponseEntity<String> fullResponse = restClient
                    .get()
                    .uri(apiUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("StackOverflow API returned client error for URL: {}", apiUrl);
                        throw new RestClientException("StackOverflow API client error");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("StackOverflow API returned server error for URL: {}", apiUrl);
                        throw new RestClientException("StackOverflow API server error");
                    })
                    .toEntity(String.class);

            StackOverflowResponse parsedResponse =
                    objectMapper.readValue(fullResponse.getBody(), StackOverflowResponse.class);

            return parsedResponse.items() != null && !parsedResponse.items().isEmpty()
                    ? Optional.of(LocalDateTime.ofEpochSecond(
                            parsedResponse.items().getFirst().lastActivityDate(), 0, ZoneOffset.UTC))
                    : Optional.empty();

        } catch (JsonProcessingException e) {
            log.error("Ошибка при обработке JSON-ответа StackOverflow", e);
            throw new StackOverflowApiException("Ошибка при обработке JSON-ответа StackOverflow", e);
        } catch (RestClientException e) {
            log.error("Ошибка при доступе к StackOverflow API", e);
            throw new StackOverflowApiException("Ошибка при доступе к StackOverflow API", e);
        }
    }
}
