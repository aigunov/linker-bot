package backend.academy.scrapper.client;

import dto.LinkUpdate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;


@ConditionalOnProperty(value="app.message.transport", havingValue="Kafka")
public class KafkaNotificationClient implements NotificationClient {
    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {

    }
}
