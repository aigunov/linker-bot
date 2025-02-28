package backend.academy.scrapper.client;

import backend.academy.scrapper.model.StackOverflowResponse;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Qualifier("stackOverflowClient")
public class StackOverflowClient extends AbstractUpdateCheckingClient {


    public StackOverflowClient(@Qualifier("trackClient") RestClient restClient,
                               LinkToApiRequestConverter converterApi) {
        super(restClient, converterApi);
    }

    @Override
    public Optional<LocalDateTime> checkUpdates(String link) {
        log.info("Checking for updates on StackOverflow: {}", link);
        var apiUrl = convertLinkToApi(link);

        ResponseEntity<String> response = restClient.get().uri(apiUrl).retrieve().toEntity(String.class);

        try {
            StackOverflowResponse parsedResponse =
                objectMapper.readValue(response.getBody(), StackOverflowResponse.class);
            log.info("StackOverflow for link {}, last update {}", link, parsedResponse);
            return parsedResponse.items() != null && !parsedResponse.items().isEmpty()
                ?
                Optional.of(LocalDateTime.ofEpochSecond(parsedResponse.items().getFirst().lastActivityDate(), 0, java.time.ZoneOffset.UTC))
                : Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Error parsing StackOverflow response", e);
            return Optional.empty();
        }
    }

    private String convertLinkToApi(String link) {
        return converterApi.convertStackOverflowUrlToApi(link);
    }
}
