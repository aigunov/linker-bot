package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.GitHubIssue;
import backend.academy.scrapper.data.dto.GitHubPullRequest;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.exception.GitHubApiException;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Comparator;
import java.util.List;
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
public class GitHubClient extends AbstractUpdateCheckingClient {

    public GitHubClient(RestClient restClient, LinkToApiRequestConverter converterApi) {
        super(restClient, converterApi);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Optional<UpdateInfo> checkUpdates(String link) {
        String apiUrl = converterApi.convertGithubUrlToApi(link);
        log.info("Checking for updates... {}", apiUrl);

        try {
            // Получаем список issues
            ResponseEntity<String> issuesResponse = restClient
                    .get()
                    .uri(apiUrl + "/issues?state=all")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("GitHub API returned client error for Issues: {}", apiUrl);
                        throw new RestClientException("GitHub API client error: " + response + " ");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("GitHub API returned server error for Issues: {}", apiUrl);
                        throw new RestClientException("GitHub API server error");
                    })
                    .toEntity(String.class);

            List<GitHubIssue> issues = objectMapper.readValue(
                    issuesResponse.getBody(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GitHubIssue.class));

            ResponseEntity<String> prsResponse = restClient
                    .get()
                    .uri(apiUrl + "/pulls?state=all")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        log.error("GitHub API returned client error for Pull Requests: {}", apiUrl);
                        throw new RestClientException("GitHub API client error");
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        log.error("GitHub API returned server error for Pull Requests: {}", apiUrl);
                        throw new RestClientException("GitHub API server error");
                    })
                    .toEntity(String.class);

            List<GitHubPullRequest> pullRequests = objectMapper.readValue(
                    prsResponse.getBody(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, GitHubPullRequest.class));

            Optional<UpdateInfo> latestIssue = issues.stream()
                    .max(Comparator.comparing(GitHubIssue::createdAt))
                    .map(issue -> UpdateInfo.builder()
                            .date(issue.createdAt())
                            .username(issue.user().login())
                            .title(issue.title())
                            .type("issue")
                            .preview(issue.body() != null ? StringUtils.substring(issue.body(), 0, 200) : "")
                            .build());

            Optional<UpdateInfo> latestPullRequest = pullRequests.stream()
                    .max(Comparator.comparing(GitHubPullRequest::createdAt))
                    .map(pr -> UpdateInfo.builder()
                            .date(pr.createdAt())
                            .username(pr.user().login())
                            .title(pr.title())
                            .type("pull-request")
                            .preview(pr.body() != null ? StringUtils.substring(pr.body(), 0, 200) : "")
                            .build());

            var latestUpdate = Stream.of(latestIssue, latestPullRequest)
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
            log.error("Error parsing GitHub response", e);
            throw new GitHubApiException("Error parsing GitHub response", e);
        } catch (RestClientException e) {
            log.error("Error accessing GitHub API", e);
            throw new GitHubApiException("Error accessing GitHub API", e);
        }
    }
}
