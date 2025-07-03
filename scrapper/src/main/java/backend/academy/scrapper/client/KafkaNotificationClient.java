package backend.academy.scrapper.client;

import dto.Digest;
import dto.LinkUpdate;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@Builder
@AllArgsConstructor
@ConditionalOnProperty(value = "app.message.transport", havingValue = "Kafka")
public class KafkaNotificationClient implements NotificationClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final WebClient webClient;

    @Qualifier("botRetry")
    private final Retry retry;

    @Qualifier("botTimeLimiter")
    private final TimeLimiter timeLimiter;

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.message.kafka.topic.digest}")
    private String digestTopic;

    private RestNotificationClient fallbackClient = null;

    /** Конструктор для Spring-инъекции */
    @Autowired
    public KafkaNotificationClient(
            KafkaTemplate<String, Object> kafkaTemplate,
            WebClient webClient,
            @Qualifier("botRetry") Retry retry,
            @Qualifier("botTimeLimiter") TimeLimiter timeLimiter,
            @Value("${app.message.kafka.topic.notification}") String notificationTopic,
            @Value("${app.message.kafka.topic.digest}") String digestTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.webClient = webClient;
        this.retry = retry;
        this.timeLimiter = timeLimiter;
        this.notificationTopic = notificationTopic;
        this.digestTopic = digestTopic;
    }

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending link update to topic {}", notificationTopic);
        try {
            kafkaTemplate.send(notificationTopic, linkUpdate);
        } catch (Exception e) {
            log.warn("Kafka send failed, switching to HTTP fallback: {}", e.getMessage());
            fallbackClient = new RestNotificationClient(webClient, retry, timeLimiter, kafkaTemplate);
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
            fallbackClient = new RestNotificationClient(webClient, retry, timeLimiter, kafkaTemplate);
            fallbackClient.sendDigest(digest);
            fallbackClient = null;
        }
    }
}
