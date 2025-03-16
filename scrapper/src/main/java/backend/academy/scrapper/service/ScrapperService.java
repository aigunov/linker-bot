package backend.academy.scrapper.service;

import backend.academy.scrapper.client.NotificationClient;
import backend.academy.scrapper.client.UpdateCheckingClient;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import backend.academy.scrapper.exception.ScrapperServicesApiException;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import dto.LinkUpdate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@SuppressWarnings(value = {"CRLF_INJECTION_LOGS"})
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperService {

    private final LinkRepository linkRepository;
    private final LinkToApiRequestConverter converter;
    private final UpdateCheckingClient stackOverflowClient;
    private final UpdateCheckingClient gitHubClient;
    private final NotificationClient notificationClient;

    @Scheduled(fixedRate = 100000)
    public void scrapper() {
        log.info("Scrapper scheduled started");
        var allLinks = linkRepository.findAll();
        for (Link link : allLinks) {
            processLink(link);
        }
    }


    private void processLink(Link link) {
        Optional<UpdateInfo> latestUpdateInfo = Optional.empty();
        try {
            if (converter.isGithubUrl(link.url())) {
                latestUpdateInfo = gitHubClient.checkUpdates(link.url());
            } else if (converter.isStackOverflowUrl(link.url())) {
                latestUpdateInfo = stackOverflowClient.checkUpdates(link.url());
            }
        } catch (ScrapperServicesApiException e) {
            log.error("Error accessing external API for link: {}", link.url(), e);
            return;
        }

        if (latestUpdateInfo.isPresent()) {
            UpdateInfo updateInfo = latestUpdateInfo.get();
            LocalDateTime latestUpdateDate = updateInfo.date();

            if (link.lastUpdate() == null || latestUpdateDate.isAfter(link.lastUpdate())) {
                log.info("Link {} updated at {}", link.url(), latestUpdateDate);
                link.lastUpdate(latestUpdateDate);
                linkRepository.save(link);
                try {
                    sendNotification(link, updateInfo.getFormattedMessage());
                } catch (BotServiceInternalErrorException e) {
                    log.error("Bot service returned INTERNAL_SERVER_ERROR for link: {}", link.url(), e);
                } catch (BotServiceException e) {
                    log.error("Failed to send notification for link: {}", link.url(), e);
                }
            }
        }

    }

    private void sendNotification(Link link, String message) {
        var update = LinkUpdate.builder()
            .id(link.id())
            .url(link.url())
            .message(message)
            .tgChatIds(link.chats().stream()
                .map(Chat::tgId).collect(Collectors.toSet()))
            .build();
        notificationClient.sendLinkUpdate(update);
    }
}
