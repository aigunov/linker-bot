package backend.academy.scrapper.config;

import java.time.Duration;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingProperties {
    private int capacity;
    private Duration duration;
}
