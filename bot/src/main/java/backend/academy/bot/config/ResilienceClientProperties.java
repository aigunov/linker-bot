package backend.academy.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "client.resilience")
public class ResilienceClientProperties {
    private ClientConfig scrapperClient;

    @Data
    public static class ClientConfig {
        private Duration timeout;
        private int maxAttempts;
        private Duration waitDuration;
        private List<Integer> retryStatuses;
    }
}
