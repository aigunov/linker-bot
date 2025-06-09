package backend.academy.scrapper.service;

import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import dto.DigestRecord;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@SuppressWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
@SuppressFBWarnings(value = {"NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE"})
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {
    private static final String REDIS_KEY_PREFIX = "digest:";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final RedisTemplate<String, DigestRecord> redisTemplate;

    /** Сохраняет уведомление для всех указанных чатов в Redis в их время дайджеста. */
    public void storeUpdate(Set<Chat> chats, Link link, UpdateInfo updateInfo) {
        for (Chat chat : chats) {
            LocalTime digestTime = chat.digestTime();
            String key = getRedisKey(digestTime);
            DigestRecord record = DigestRecord.builder()
                    .url(link.url())
                    .chatId(chat.tgId())
                    .linkId(link.id())
                    .message(updateInfo.getFormattedMessage())
                    .build();

            redisTemplate.opsForList().rightPush(key, record);
            log.debug("Stored digest for time {} in key {}: {}", digestTime, key, record);
        }
    }

    /** Извлекает все уведомления для текущего времени (hh:mm) и группирует их по chatId. */
    public Map<Long, List<DigestRecord>> consumeForTime(LocalTime now) {
        String key = getRedisKey(now);
        List<DigestRecord> rawRecords = redisTemplate.opsForList().range(key, 0, -1);

        if (rawRecords.isEmpty()) {
            return Collections.emptyMap();
        }

        return rawRecords.stream()
                .filter(Objects::nonNull)
                .map(DigestRecord.class::cast)
                .collect(Collectors.groupingBy(DigestRecord::chatId));
    }

    public void clearDigestTimeKey(LocalTime now) {
        String key = getRedisKey(now);
        redisTemplate.delete(key);
        log.info("Cleared digest key for time {}: {}", now, key);
    }

    private String getRedisKey(LocalTime time) {
        return REDIS_KEY_PREFIX + time.format(TIME_FORMATTER);
    }
}
