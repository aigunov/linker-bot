package backend.academy.scrapper.client;

import dto.ErrorUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public final class KafkaDLQNotificationClient {

    private final KafkaTemplate<String, Object> kafka;

    @Value("${app.message.kafka.topic.dead-letter}")
    private String dlqTopic;

    public void send(final ErrorUpdate error){
        log.info("Sending error update {} to dead-letter topic", error);
        kafka.send(dlqTopic, error);
    }
}
