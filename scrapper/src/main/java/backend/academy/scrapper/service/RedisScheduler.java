package backend.academy.scrapper.service;

import backend.academy.scrapper.client.NotificationClient;
import dto.Digest;
import dto.DigestRecord;
import dto.LinkUpdate;
import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisScheduler {

    private final RedisService redisService;
    private final NotificationClient notificationClient;

    private ExecutorService executor;

    @Value("${app.digest.threads-count}")
    private int threadsCount;

    @PostConstruct
    public void init() {
        this.executor = Executors.newFixedThreadPool(threadsCount);
    }

    @Scheduled(fixedDelayString = "${app.digest.scheduler-time}")
    private void dispatchDigest() {
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        var chatToRecord = redisService.consumeForTime(now);

        if (chatToRecord.isEmpty()) {
            log.info("No digest records to dispatch at {}", now);
            return;
        }

        List<Map.Entry<Long, List<DigestRecord>>> allEntries = new ArrayList<>(chatToRecord.entrySet());
        int batchSize = (int) Math.ceil((double) allEntries.size() / threadsCount);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadsCount; i++) {
            int start = i * batchSize;
            int end = Math.min(start + batchSize, allEntries.size());

            if (start >= end) break;

            var subList = allEntries.subList(start, end);
            futures.add(executor.submit(() -> sendDigest(subList)));
        }

        for (var future: futures){
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sendDigest(List<Map.Entry<Long, List<DigestRecord>>> entries) {
        for (var entry : entries) {
            var updates = entry.getValue().stream()
                .map(this::convertDigesttoLinkUpdate)
                .toList();
            var digest = Digest.builder().tgId(entry.getKey()).updates(updates).build();

            try {
                notificationClient.sendDigest(digest);
                log.info("Sent digest to chat {} ", digest);
            } catch (Exception e) {
                log.error("Failed to send digest to chat {}: {}", entry.getKey(), e.getMessage(), e);
            }
        }
    }

    private LinkUpdate convertDigesttoLinkUpdate(DigestRecord digest) {
        return LinkUpdate.builder()
            .id(digest.linkId())
            .tgChatIds(Set.of(digest.chatId()))
            .url(digest.url())
            .message(digest.message())
            .build();
    }
}

