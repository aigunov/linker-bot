package backend.academy.bot.service;

import dto.AddLinkRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddLinkRequestService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "linkRequests";

    public void createLinkRequest(Long chatId, String uri){
        AddLinkRequest request = AddLinkRequest.builder()
            .uri(uri)
            .tags(new ArrayList<>())
            .filters(new ArrayList<>())
            .build();
        redisTemplate.opsForValue().set(PREFIX + chatId, request);
    }

    public AddLinkRequest getLinkRequest(Long chatId) {
        return (AddLinkRequest) redisTemplate.opsForValue().get(PREFIX + chatId);
    }

    public void updateLinkRequestTags(Long chatId, String tags) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            var req = getLinkRequest(chatId);
            if (req != null){
                req.tags().addAll(List.of(tags.split(" ")));
                redisTemplate.opsForValue().set(PREFIX + chatId, req);
            }
            return null;
        });
    }

    public void updateLinkRequestFilters(Long chatId, String filters) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            AddLinkRequest req = getLinkRequest(chatId);
            if (req != null) {
                req.filters().addAll(List.of(filters.split(" ")));
                redisTemplate.opsForValue().set(PREFIX + chatId, req);
            }
            return null;
        });
    }

    public void clearLinkRequest(Long chatId) {
        redisTemplate.delete(PREFIX + chatId);
    }
}
