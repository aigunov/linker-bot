package backend.academy.scrapper.config;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Component
@ConfigurationProperties(prefix = "client.resilience")
public class ResilienceClientProperties {
    private ClientConfig githubClient;
    private ClientConfig stackoverflowClient;
    private ClientConfig botClient;

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class ClientConfig {
        private Duration timeout;
        private int maxAttempts;
        private Duration waitDuration;
        private List<Integer> retryStatuses;
        private int rateLimitForPeriod;
        private Duration rateLimitRefreshPeriod;
    }
}
