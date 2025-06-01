package backend.academy.scrapper.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.resilience.bot-client")
public record BotClientProperties(
    Duration timeout,
    Integer maxAttempts,
    Duration waitDuration,
    List<Integer> retryStatuses,
    Integer rateLimitForPeriod,
    Duration rateLimitRefreshPeriod
) {
}
