package backend.academy.scrapper.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.resilience.stackoverflow-client")
public record StackoverflowClientProperties(
    Duration timeout,
    Integer maxAttempts,
    Duration waitDuration,
    List<Integer> retryStatuses,
    Integer rateLimitForPeriod,
    Duration rateLimitRefreshPeriod
) {
}
