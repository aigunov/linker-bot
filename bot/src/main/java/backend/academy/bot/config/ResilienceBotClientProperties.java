package backend.academy.bot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;

@Getter
@RequiredArgsConstructor
@Component
@ConfigurationProperties(prefix = "client.resilience-bot")
public class ResilienceBotClientProperties {
    private ClientConfig scrapperClient;

    @Getter
    @RequiredArgsConstructor
    public static class ClientConfig {
        private Duration timeout;
        private int maxAttempts;
        private Duration waitDuration;
        private List<Integer> retryStatuses;
    }
}
