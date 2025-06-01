package backend.academy.scrapper.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

        registry.retry("githubClient", RetryConfig.custom()
            .maxAttempts(github.maxAttempts())
            .waitDuration(github.waitDuration())
            .retryOnResult(response -> response instanceof ResponseEntity<?> r &&
                github.retryStatuses().contains(r.getStatusCode().value()))
            .retryExceptions(Exception.class)
            .build());

        registry.retry("stackoverflowClient", RetryConfig.custom()
            .maxAttempts(stackoverflow.maxAttempts())
            .waitDuration(stackoverflow.waitDuration())
            .retryOnResult(response -> response instanceof ResponseEntity<?> r &&
                stackoverflow.retryStatuses().contains(r.getStatusCode().value()))
            .retryExceptions(Exception.class)
            .build());

        registry.retry("botClient", RetryConfig.custom()
            .maxAttempts(bot.maxAttempts())
            .waitDuration(bot.waitDuration())
            .retryOnException(e ->
                e instanceof WebClientResponseException ex &&
                    bot.retryStatuses().contains(ex.getStatusCode().value()))
            .build());

        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();

        registry.timeLimiter("githubClient", TimeLimiterConfig.custom()
            .timeoutDuration(github.timeout())
            .build());

        registry.timeLimiter("stackoverflowClient", TimeLimiterConfig.custom()
            .timeoutDuration(stackoverflow.timeout())
            .build());

        registry.timeLimiter("botClient", TimeLimiterConfig.custom()
            .timeoutDuration(bot.timeout())
            .build());

        return registry;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
