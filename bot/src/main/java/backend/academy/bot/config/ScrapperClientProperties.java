package backend.academy.bot.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.resilience-bot.scrapper-client")
public record ScrapperClientProperties(
        Duration timeout, Integer maxAttempts, Duration waitDuration, List<Integer> retryStatuses) {}
