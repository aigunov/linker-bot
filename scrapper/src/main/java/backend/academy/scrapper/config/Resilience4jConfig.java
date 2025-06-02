package backend.academy.scrapper.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private final GithubClientProperties github;
    private final StackoverflowClientProperties stackoverflow;
    private final BotClientProperties bot;

    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();

        registry.addConfiguration("githubRetryConfig", RetryConfig.custom()
            .maxAttempts(github.maxAttempts())
            .waitDuration(github.waitDuration())
            .retryOnResult(response -> response instanceof org.springframework.http.ResponseEntity<?> r &&
                github.retryStatuses().contains(r.getStatusCode().value()))
            .retryExceptions(Exception.class)
            .build());

        registry.addConfiguration("stackoverflowRetryConfig", RetryConfig.custom()
            .maxAttempts(stackoverflow.maxAttempts())
            .waitDuration(stackoverflow.waitDuration())
            .retryOnResult(response -> response instanceof org.springframework.http.ResponseEntity<?> r &&
                stackoverflow.retryStatuses().contains(r.getStatusCode().value()))
            .retryExceptions(Exception.class)
            .build());

        registry.addConfiguration("botRetryConfig", RetryConfig.custom()
            .maxAttempts(bot.maxAttempts())
            .waitDuration(bot.waitDuration())
            .retryOnException(e ->
                e instanceof org.springframework.web.reactive.function.client.WebClientResponseException ex &&
                    bot.retryStatuses().contains(ex.getStatusCode().value()))
            .build());

        return registry;
    }

    @Bean
    public Retry githubRetry(RetryRegistry registry) {
        return registry.retry("githubClient", "githubRetryConfig");
    }

    @Bean
    public Retry stackoverflowRetry(RetryRegistry registry) {
        return registry.retry("stackoverflowClient", "stackoverflowRetryConfig");
    }

    @Bean
    public Retry botRetry(RetryRegistry registry) {
        return registry.retry("botClient", "botRetryConfig");
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();

        registry.addConfiguration("githubTimeLimiter", TimeLimiterConfig.custom()
            .timeoutDuration(github.timeout())
            .cancelRunningFuture(false)
            .build());

        registry.addConfiguration("stackoverflowTimeLimiter", TimeLimiterConfig.custom()
            .timeoutDuration(stackoverflow.timeout())
            .cancelRunningFuture(false)
            .build());

        registry.addConfiguration("botTimeLimiter", TimeLimiterConfig.custom()
            .timeoutDuration(bot.timeout())
            .cancelRunningFuture(false)
            .build());

        return registry;
    }

    @Bean
    public TimeLimiter githubTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("githubClient", "githubTimeLimiter");
    }

    @Bean
    public TimeLimiter stackoverflowTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("stackoverflowClient", "stackoverflowTimeLimiter");
    }

    @Bean
    public TimeLimiter botTimeLimiter(TimeLimiterRegistry registry) {
        return registry.timeLimiter("botClient", "botTimeLimiter");
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public CircuitBreaker githubCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("githubClient");
    }

    @Bean
    public CircuitBreaker stackoverflowCircuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("stackoverflowClient");
    }


}
