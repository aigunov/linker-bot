package backend.academy.scrapper.client;

import dto.ErrorUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class KafkaDLQNotificationClient {

    public void send(final ErrorUpdate error){

    }
}
