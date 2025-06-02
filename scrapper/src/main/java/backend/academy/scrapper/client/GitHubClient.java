package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.GitHubIssue;
import backend.academy.scrapper.data.dto.GitHubPullRequest;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
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
public class GitHubClient extends AbstractUpdateCheckingClient {

    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;

    public GitHubClient(
        RestClient restClient,
        LinkToApiRequestConverter converterApi,
        @Qualifier("githubCircuitBreaker") CircuitBreaker circuitBreaker,
        @Qualifier("githubRetry") Retry retry,
        @Qualifier("githubTimeLimiter") TimeLimiter timeLimiter
    ) {
        super(restClient, converterApi);
        this.objectMapper.registerModule(new JavaTimeModule());
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.timeLimiter = timeLimiter;
    }

    @Override
    public Optional<UpdateInfo> checkUpdates(String link) throws JsonProcessingException {
        String apiUrl = converterApi.convertGithubUrlToApi(link);
        log.info("Checking GitHub updates for: {}", apiUrl);

        Supplier<Optional<UpdateInfo>> decoratedSupplier = Decorators.ofSupplier(() -> fetchWithResilience(apiUrl))
            .withRetry(retry)
            .withCircuitBreaker(circuitBreaker)
            .withFallback(List.of(Throwable.class), t -> {
                log.warn("Fallback executed for GitHubClient: {}", t.getMessage());
                return Optional.empty();
            })
            .decorate();

        return decoratedSupplier.get();
    }

    private Optional<UpdateInfo> fetchWithResilience(String apiUrl) {
        try {
            return timeLimiter.executeFutureSupplier(() -> CompletableFuture.supplyAsync(() -> {
                try {
                    List<GitHubIssue> issues = fetchIssues(apiUrl);
                    List<GitHubPullRequest> prs = fetchPullRequests(apiUrl);
                    return determineLatestUpdate(issues, prs);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }));
        } catch (Exception e) {
            throw new CompletionException(e);
        }
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
