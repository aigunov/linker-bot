package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.GitHubApiException;
import backend.academy.scrapper.model.GitHubResponse;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Slf4j
public class GitHubClient extends AbstractUpdateCheckingClient {

    public GitHubClient(RestClient restClient, LinkToApiRequestConverter converterApi) {
        super(restClient, converterApi);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Optional<LocalDateTime> checkUpdates(String link) {
        String apiUrl = converterApi.convertGithubUrlToApi(link);
        log.info("Checking for updates... {}", apiUrl);

        try {
            ResponseEntity<String> rawResponse = restClient
                    .get()
                    .uri(apiUrl)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("GitHub API returned client error for URL: {}", apiUrl);
                        throw new RestClientException("GitHub API client error");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("GitHub API returned server error for URL: {}", apiUrl);
                        throw new RestClientException("GitHub API server error");
                    })
                    .toEntity(String.class);

            GitHubResponse parsedResponse = objectMapper.readValue(rawResponse.getBody(), GitHubResponse.class);
            log.info(MessageFormat.format("last update {0}", parsedResponse.updatedAt()));
            return Optional.of(parsedResponse.updatedAt());

        } catch (JsonProcessingException e) {
            log.error("Error parsing GitHub response", e);
            throw new GitHubApiException("Error parsing GitHub response", e);
        } catch (RestClientException e) {
            log.error("Error accessing GitHub API", e);
            throw new GitHubApiException("Error accessing GitHub API", e);
        }
    }
}
