package backend.academy.bot.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import java.time.Duration;
import java.util.function.Predicate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Resilience4jConfig {

    private ResilienceClientProperties properties;

    @Bean
    public RetryRegistry retryRegistry() {
        Predicate<Throwable> shouldRetryException = throwable -> {
            if (throwable instanceof HttpServerErrorException e) {
                int statusCode = e.getStatusCode().value();
                boolean shouldRetry = properties.scrapperClient().retryStatuses().contains(statusCode);
                if (shouldRetry) {
                    log.warn("Retrying due to server error: {}", statusCode);
                }
                return shouldRetry;
            }
            return false;
        };

        RetryConfig config = RetryConfig.custom()
            .maxAttempts(properties.scrapperClient().maxAttempts())
            .waitDuration(properties.scrapperClient().waitDuration())
            .retryOnResult(response -> {
                if (response instanceof ResponseEntity<?> r) {
                    boolean match = properties.scrapperClient().retryStatuses()
                        .contains(r.getStatusCode().value());
                    if (match) {
                        log.warn("Retrying due to response status: {}", r.getStatusCode());
                    }
                    return match;
                }
                return false;
            })
            .retryOnException(shouldRetryException)
            .build();

        RetryRegistry registry = RetryRegistry.ofDefaults();
        registry.retry("scrapperClient", config);
        return registry;
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(1)
            .minimumNumberOfCalls(1)
            .failureRateThreshold(100.0f)
            .permittedNumberOfCallsInHalfOpenState(1)
            .waitDurationInOpenState(Duration.ofSeconds(1))
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordException(throwable -> {
                log.warn("CircuitBreaker recording failure: {}", throwable.toString());
                return true;
            })
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        registry.circuitBreaker("scrapperClient", config);
        return registry;
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
            .timeoutDuration(properties.scrapperClient().timeout())
            .build();

        TimeLimiterRegistry registry = TimeLimiterRegistry.ofDefaults();
        registry.timeLimiter("scrapperClient", config);
        return registry;
    }
}
