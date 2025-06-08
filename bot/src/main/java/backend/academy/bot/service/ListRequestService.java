package backend.academy.bot.service;

import dto.GetLinksRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListRequestService {
    private final RedisTemplate<String, GetLinksRequest> redisTemplate;
    private static final String PREFIX = "listRequest";

    public void createListRequest(final Long chatId) {
        var request = GetLinksRequest.builder()
                .tags(new ArrayList<>())
                .filters(new ArrayList<>())
                .build();
        redisTemplate.opsForValue().set(PREFIX + chatId, request);
    }

    public void updateListRequestTags(Long chatId, String tags) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var req = getListRequest(chatId);
            if (req != null) {
                req.tags().addAll(List.of(tags.split(" ")));
                redisTemplate.opsForValue().set(PREFIX + chatId, req);
            }
            return null;
        });
    }

    public void updateListRequestFilters(Long chatId, String filters) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var req = getListRequest(chatId);
            if (req != null) {
                req.filters().addAll(List.of(filters.split(" ")));
                redisTemplate.opsForValue().set(PREFIX + chatId, req);
            }
            return null;
        });
    }

    public GetLinksRequest getListRequest(Long chatId) {
        return (GetLinksRequest) redisTemplate.opsForValue().get(PREFIX + chatId);
    }

    public void clearLinkRequest(Long chatId) {
        redisTemplate.delete(PREFIX + chatId);
    }
}
