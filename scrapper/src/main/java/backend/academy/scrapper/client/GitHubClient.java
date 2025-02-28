package backend.academy.scrapper.client;

import backend.academy.scrapper.model.GitHubResponse;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@Qualifier("gitHubClient")
public class GitHubClient extends AbstractUpdateCheckingClient {


    public GitHubClient(@Qualifier("trackClient") RestClient restClient,
                        LinkToApiRequestConverter converterApi) {
        super(restClient, converterApi);
    }

    @Override
    public Optional<LocalDateTime> checkUpdates(String link) {
        log.info("Checking for updates on GitHub: {}", link);
        var apiUrl = convertLinkToApi(link);

        ResponseEntity<String> response = restClient.get().uri(apiUrl).retrieve().toEntity(String.class);

        try {
            GitHubResponse gitHubResponse = objectMapper.readValue(response.getBody(), GitHubResponse.class);
            log.info(MessageFormat.format("GitHub for link {}, last update {0}", link, gitHubResponse.updatedAt()));
            return Optional.ofNullable(gitHubResponse.getUpdatedAt());
        } catch (JsonProcessingException e) {
            log.error("Error parsing GitHub response", e);
            return Optional.empty();
        }
    }

    private String convertLinkToApi(String link) {
        return converterApi.convertGithubUrlToApi(link);
    }
}

