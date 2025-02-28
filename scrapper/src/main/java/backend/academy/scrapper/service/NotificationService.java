package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final RestClient restClient;

    public void sendLinkUpdate(LinkUpdate linkUpdate) {
        log.info("Sending notification for link update: {}", linkUpdate);
        try {
            restClient.post()
                .uri("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(linkUpdate)
                .retrieve()
                .toBodilessEntity();
            log.info("Notification sent successfully: {}", linkUpdate);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                log.error("Bot service returned INTERNAL_SERVER_ERROR for update: {}", linkUpdate);
                throw new BotServiceInternalErrorException("Bot service returned INTERNAL_SERVER_ERROR", e);
            } else {
                log.error("Failed to send notification: {}", linkUpdate, e);
                throw new BotServiceException("Failed to send notification", e);
            }
        } catch (RestClientException e) {
            log.error("Failed to send notification: {}", linkUpdate, e);
            throw new BotServiceException("Failed to send notification", e);
        }
    }
}
