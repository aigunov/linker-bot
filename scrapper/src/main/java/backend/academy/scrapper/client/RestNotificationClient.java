package backend.academy.scrapper.client;

import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value="app.message.transport", havingValue="HTTP")
public class RestNotificationClient implements NotificationClient{

    private final WebClient webClient;

    @Override
    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending notification for link update: {}", linkUpdate);
        webClient
                .post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(linkUpdate), LinkUpdate.class)
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.OK)) {
                        log.info("Notification sent successfully: {}", linkUpdate);
                        return Mono.empty();
                    } else if (clientResponse.statusCode().equals(HttpStatus.INTERNAL_SERVER_ERROR)) {
                        log.error("Bot service returned INTERNAL_SERVER_ERROR for update: {}", linkUpdate);
                        return Mono.error(
                                new BotServiceInternalErrorException("Bot service returned INTERNAL_SERVER_ERROR"));
                    } else {
                        log.error("Failed to send notification: {}", linkUpdate);
                        return Mono.error(new BotServiceInternalErrorException("Failed to send notification"));
                    }
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                        log.error("Bot service returned INTERNAL_SERVER_ERROR for update: {}", linkUpdate);
                        return Mono.error(
                                new BotServiceInternalErrorException("Bot service returned INTERNAL_SERVER_ERROR", e));
                    } else {
                        log.error("Failed to send notification: {}", linkUpdate, e);
                        return Mono.error(new BotServiceException("Failed to send notification", e));
                    }
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Failed to send notification: {}", linkUpdate, e);
                    return Mono.error(new BotServiceException("Failed to send notification", e));
                })
                .subscribe();
    }

    @Override
    public void sendDigest(List<LinkUpdate> linkUpdates) {

    }
}
