package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.GitHubIssue;
import backend.academy.scrapper.data.dto.GitHubPullRequest;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        CircuitBreakerRegistry circuitBreakerRegistry,
        RetryRegistry retryRegistry,
        TimeLimiterRegistry timeLimiterRegistry
    ) {
        super(restClient, converterApi);
        objectMapper.registerModule(new JavaTimeModule());

        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("gitHubClient");
        this.retry = retryRegistry.retry("gitHubClient");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("gitHubClient");
    }

    @Override
    public Optional<UpdateInfo> checkUpdates(String link) throws JsonProcessingException {
        String apiUrl = converterApi.convertGithubUrlToApi(link);
        log.info("Checking for updates... {}", apiUrl);

        Supplier<CompletableFuture<Optional<UpdateInfo>>> supplier = () -> CompletableFuture.supplyAsync(() -> {
            try {
                List<GitHubIssue> issues = fetchIssues(apiUrl);
                List<GitHubPullRequest> pullRequests = fetchPullRequests(apiUrl);
                return determineLatestUpdate(issues, pullRequests);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        Supplier<Optional<UpdateInfo>> decoratedSupplier = Decorators.ofSupplier(() -> {
                try {
                    return timeLimiter.executeFutureSupplier(supplier);
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            })
            .withRetry(retry)
            .withCircuitBreaker(circuitBreaker)
            .withFallback(List.of(Throwable.class), t -> {
                log.warn("Fallback executed for GitHubClient due to: {}", t.getMessage());
                return Optional.empty();
            })
            .decorate();

        return decoratedSupplier.get();
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
