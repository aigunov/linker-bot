package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.StackOverflowResponse;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.exception.StackOverflowApiException;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
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
    public Optional<UpdateInfo> checkUpdates(String link) {
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

            if (parsedResponse.items() == null || parsedResponse.items().isEmpty()) {
                return Optional.empty();
            }

            StackOverflowResponse.StackOverflowItem item =
                    parsedResponse.items().getFirst();
            String questionTitle = item.title();

            Optional<UpdateInfo> latestAnswer = item.answers() != null
                            && !item.answers().isEmpty()
                    ? item.answers().stream()
                            .max(Comparator.comparingLong(StackOverflowResponse.StackOverflowItem.Answer::creationDate))
                            .map(answer -> UpdateInfo.builder()
                                    .date(LocalDateTime.ofEpochSecond(answer.creationDate(), 0, ZoneOffset.UTC))
                                    .title(questionTitle)
                                    .username(answer.owner().displayName())
                                    .type("answer")
                                    .preview(StringUtils.substring(answer.body(), 0, 200))
                                    .build())
                    : Optional.empty();

            Optional<UpdateInfo> latestComment = item.comments() != null
                            && !item.comments().isEmpty()
                    ? item.comments().stream()
                            .max(Comparator.comparingLong(
                                    StackOverflowResponse.StackOverflowItem.Comment::creationDate))
                            .map(comment -> UpdateInfo.builder()
                                    .date(LocalDateTime.ofEpochSecond(comment.creationDate(), 0, ZoneOffset.UTC))
                                    .title(questionTitle)
                                    .username(comment.owner().displayName())
                                    .type("comment")
                                    .preview(StringUtils.substring(comment.body(), 0, 200))
                                    .build())
                    : Optional.empty();

            Optional<UpdateInfo> latestUpdate = Stream.of(latestAnswer, latestComment)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(Comparator.comparing(UpdateInfo::date));

            if (latestUpdate.isPresent()) {
                var update = latestUpdate.get();
                log.info("Latest {} update on link ({}), \n is: {}", update.type(), link, update);
                return latestUpdate;
            }
            return Optional.empty();

        } catch (JsonProcessingException e) {
            log.error("Ошибка при обработке JSON-ответа StackOverflow", e);
            throw new StackOverflowApiException("Ошибка при обработке JSON-ответа StackOverflow", e);
        } catch (RestClientException e) {
            log.error("Ошибка при доступе к StackOverflow API", e);
            throw new StackOverflowApiException("Ошибка при доступе к StackOverflow API", e);
        }
    }
}
