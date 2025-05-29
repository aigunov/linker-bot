package backend.academy.scrapper.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private final ResilienceClientProperties properties;

    @Bean
    public RetryRegistry retryRegistry() {
        RetryRegistry registry = RetryRegistry.ofDefaults();

        registry.retry("githubClient", RetryConfig.custom()
            .maxAttempts(properties.githubClient().maxAttempts())
            .waitDuration(properties.githubClient().waitDuration())
            .retryOnResult(response -> {
                if (response instanceof org.springframework.http.ResponseEntity<?> r) {
                    return properties.githubClient().retryStatuses().contains(r.getStatusCode().value());
                }
                return false;
            })
            .retryExceptions(Exception.class)
            .build());

        registry.retry("stackoverflowClient", RetryConfig.custom()
            .maxAttempts(properties.stackoverflowClient().maxAttempts())
            .waitDuration(properties.stackoverflowClient().waitDuration())
            .retryOnResult(response -> {
                if (response instanceof org.springframework.http.ResponseEntity<?> r) {
                    return properties.stackoverflowClient().retryStatuses().contains(r.getStatusCode().value());
                }
                return false;
            })
            .retryExceptions(Exception.class)
            .build());

        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();

        registry.timeLimiter("githubClient", TimeLimiterConfig.custom()
            .timeoutDuration(properties.githubClient().timeout())
            .build());

        registry.timeLimiter("stackoverflowClient", TimeLimiterConfig.custom()
            .timeoutDuration(properties.stackoverflowClient().timeout())
            .build());

        return registry;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
