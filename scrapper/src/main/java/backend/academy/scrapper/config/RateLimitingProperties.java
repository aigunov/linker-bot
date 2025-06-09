package backend.academy.scrapper.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rate-limiting")
public record RateLimitingProperties(int capacity, Duration duration) {}
