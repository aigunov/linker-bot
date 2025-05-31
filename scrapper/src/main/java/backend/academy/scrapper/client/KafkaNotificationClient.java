package backend.academy.scrapper.client;

import dto.Digest;
import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.message.transport", havingValue = "Kafka")
public class KafkaNotificationClient implements NotificationClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Lazy
    private final RestNotificationClient httpFallbackClient;

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.message.kafka.topic.digest}")
    private String digestTopic;

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending link update to topic {}", notificationTopic);
        try {
            kafkaTemplate.send(notificationTopic, linkUpdate);
        } catch (Exception e) {
            log.warn("Kafka send failed, switching to HTTP fallback: {}", e.getMessage());
            httpFallbackClient.sendLinkUpdate(linkUpdate);
        }
    }

    @Override
    public void sendDigest(Digest digest) {
        log.info("Sending digest to topic {}", digestTopic);
        try {
            kafkaTemplate.send(digestTopic, digest);
        } catch (Exception e) {
            log.warn("Kafka send failed, switching to HTTP fallback: {}", e.getMessage());
            httpFallbackClient.sendDigest(digest);
        }
    }
}
