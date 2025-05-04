package backend.academy.scrapper.client;

import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value="app.message.transport", havingValue="Kafka")
public class KafkaNotificationClient implements NotificationClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending link update to topic {}", notificationTopic);
        kafkaTemplate.send(notificationTopic, linkUpdate);
    }
}
