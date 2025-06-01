package backend.academy.bot.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "client.resilience-bot.scrapper-client")
public record ScrapperClientProperties(
    Duration timeout,
    Integer maxAttempts,
    Duration waitDuration,
    List<Integer> retryStatuses
) {
}
