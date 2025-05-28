package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.GitHubIssue;
import backend.academy.scrapper.data.dto.GitHubPullRequest;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Comparator;
import java.util.List;
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
public class GitHubClient extends AbstractUpdateCheckingClient {

    public GitHubClient(RestClient restClient, LinkToApiRequestConverter converterApi) {
        super(restClient, converterApi);
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Override
    @Retry(name = "gitHubClient", fallbackMethod = "fallback")
    @TimeLimiter(name = "gitHubClient")
    @CircuitBreaker(name = "gitHubClient", fallbackMethod = "fallback")
    public Optional<UpdateInfo> checkUpdates(String link) throws JsonProcessingException {
        String apiUrl = converterApi.convertGithubUrlToApi(link);
        log.info("Checking for updates... {}", apiUrl);

        try {
            List<GitHubIssue> issues = fetchIssues(apiUrl);
            List<GitHubPullRequest> pullRequests = fetchPullRequests(apiUrl);
            return determineLatestUpdate(issues, pullRequests);
        } catch (RestClientException e) {
            log.error("RestClientException occurred while checking updates for link {}: {}", link, e.getMessage());
            throw e;
        }
    }

    public Optional<UpdateInfo> fallback(String link, Throwable t) {
        log.warn("GitHubClient fallback executed for {} due to: {}", link, t.toString());
        return Optional.empty();
    }

    private List<GitHubIssue> fetchIssues(String apiUrl) throws JsonProcessingException {
        var response = restClient.get()
            .uri(apiUrl + "/issues?state=all")
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                log.error("GitHub API returned error for Issues [{}]: status={}", apiUrl, res.getStatusCode());
                throw new RestClientException("GitHub API error for issues");
            })
            .toEntity(String.class);

        return objectMapper.readValue(response.getBody(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, GitHubIssue.class));
    }

    private List<GitHubPullRequest> fetchPullRequests(String apiUrl) throws JsonProcessingException {
        var response = restClient.get()
            .uri(apiUrl + "/pulls?state=all")
            .retrieve()
            .onStatus(HttpStatusCode::isError, (req, res) -> {
                log.error("GitHub API returned error for Pull Requests [{}]: status={}", apiUrl, res.getStatusCode());
                throw new RestClientException("GitHub API error for pull requests");
            })
            .toEntity(String.class);

        return objectMapper.readValue(response.getBody(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, GitHubPullRequest.class));
    }

    private Optional<UpdateInfo> determineLatestUpdate(List<GitHubIssue> issues, List<GitHubPullRequest> pullRequests) {
        var latestIssue = issues.stream()
            .max(Comparator.comparing(GitHubIssue::createdAt))
            .map(issue -> UpdateInfo.builder()
                .date(issue.createdAt())
                .username(issue.user().login())
                .title(issue.title())
                .type("issue")
                .preview(Optional.ofNullable(issue.body()).map(b -> StringUtils.substring(b, 0, 200)).orElse(""))
                .build());

        var latestPR = pullRequests.stream()
            .max(Comparator.comparing(GitHubPullRequest::createdAt))
            .map(pr -> UpdateInfo.builder()
                .date(pr.createdAt())
                .username(pr.user().login())
                .title(pr.title())
                .type("pull-request")
                .preview(Optional.ofNullable(pr.body()).map(b -> StringUtils.substring(b, 0, 200)).orElse(""))
                .build());

        return Stream.of(latestIssue, latestPR)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .max(Comparator.comparing(UpdateInfo::date));
    }
}
