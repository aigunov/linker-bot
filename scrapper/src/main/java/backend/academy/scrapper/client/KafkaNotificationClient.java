package backend.academy.scrapper.client;

import dto.Digest;
import dto.LinkUpdate;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.message.transport", havingValue = "Kafka")
public class KafkaNotificationClient implements NotificationClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WebClient webClient;
    private final RetryRegistry retryRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.message.kafka.topic.digest}")
    private String digestTopic;

    private RestNotificationClient fallbackClient = null;

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending link update to topic {}", notificationTopic);
        try {
            kafkaTemplate.send(notificationTopic, linkUpdate);
        } catch (Exception e) {
            log.warn("Kafka send failed, switching to HTTP fallback: {}", e.getMessage());
            fallbackClient = new RestNotificationClient(webClient, retryRegistry, timeLimiterRegistry, kafkaTemplate);
            fallbackClient.sendLinkUpdate(linkUpdate);
            fallbackClient = null;
        }
    }

    @Override
    public void sendDigest(Digest digest) {
        log.info("Sending digest to topic {}", digestTopic);
        try {
            kafkaTemplate.send(digestTopic, digest);
        } catch (Exception e) {
            log.warn("Kafka send failed, switching to HTTP fallback: {}", e.getMessage());
            fallbackClient = new RestNotificationClient(webClient, retryRegistry, timeLimiterRegistry, kafkaTemplate);
            fallbackClient.sendDigest(digest);
            fallbackClient = null;
        }
    }
}
