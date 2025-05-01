package backend.academy.scrapper.service;

import backend.academy.scrapper.client.NotificationClient;
import backend.academy.scrapper.client.UpdateCheckingClient;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.exception.BotServiceException;
import backend.academy.scrapper.exception.BotServiceInternalErrorException;
import backend.academy.scrapper.exception.ScrapperServicesApiException;
import backend.academy.scrapper.repository.link.LinkRepository;
import dto.LinkUpdate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@SuppressWarnings(value = {"CRLF_INJECTION_LOGS"})
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
@Slf4j
@Service
@RequiredArgsConstructor
public class ScrapperService {

    @Value("${app.scrapper.page-size:1000}")
    private int pageSize;

    @Value("${app.scrapper.threads-count:4}")
    private int threadsCount;

    private final LinkRepository linkRepository;
    private final LinkToApiRequestConverter converter;
    private final UpdateCheckingClient stackOverflowClient;
    private final UpdateCheckingClient gitHubClient;
    private final NotificationClient notificationClient;

    private ExecutorService executorService;

    @PostConstruct
    public void initializeExecutor() {
        this.executorService = Executors.newFixedThreadPool(this.threadsCount);
    }

    @Scheduled(fixedDelayString = "${app.scrapper.scheduled-time:100000}")
    public void scrapper() {
        log.info("Scrapper scheduled started");
        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Iterable<Link> linksIterable;

        do {
            linksIterable = linkRepository.findAllWithChats(pageable);
            List<Link> links =
                    StreamSupport.stream(linksIterable.spliterator(), false).collect(Collectors.toList());
            if (!links.isEmpty()) {
                List<Future<?>> futures = processBatchInParallel(links);
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (NullPointerException | InterruptedException | ExecutionException e) {
                        log.error("Error in processing link batch", e);
                    }
                }
            }
            pageNumber++;
            pageable = PageRequest.of(pageNumber, pageSize);
        } while (linksIterable.iterator().hasNext());
    }

    private List<Future<?>> processBatchInParallel(Iterable<Link> links) {
        List<Future<?>> futures = new ArrayList<>();
        Spliterator<Link> spliterator = links.spliterator();

        for (int i = 0; i < threadsCount; i++) {
            Spliterator<Link> chunkSpliterator = (i < threadsCount - 1) ? spliterator.trySplit() : spliterator;

            futures.add(executorService.submit(() -> {
                StreamSupport.stream(chunkSpliterator, false).forEach(this::processLink);
            }));
        }

        return futures;
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
                .tgChatIds(link.chats().stream().map(Chat::tgId).collect(Collectors.toSet()))
                .build();
        notificationClient.sendLinkUpdate(update);
    }
}
