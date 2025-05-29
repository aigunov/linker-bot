package backend.academy.scrapper.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingProperties {
    private int capacity;
    private Duration duration;
}
