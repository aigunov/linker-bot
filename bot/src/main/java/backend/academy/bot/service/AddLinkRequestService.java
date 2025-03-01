package backend.academy.bot.service;

import dto.AddLinkRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AddLinkRequestService {
    private final Map<Long, AddLinkRequest> linkRequests = new ConcurrentHashMap<>();

    public void createLinkRequest(Long chatId, String uri) {
        linkRequests.put(chatId, AddLinkRequest.builder().uri(uri).build());
    }

    public AddLinkRequest getLinkRequest(Long chatId) {
        return linkRequests.get(chatId);
    }

    public void updateLinkRequestTags(Long chatId, String tags) {
        linkRequests.computeIfPresent(chatId, (k, v) -> {
            v.tags().addAll(List.of(tags.split(" ")));
            return v;
        });
    }

    public void updateLinkRequestFilters(Long chatId, String filters) {
        linkRequests.computeIfPresent(chatId, (k, v) -> {
            v.filters().addAll(List.of(filters.split(" ")));
            return v;
        });
    }

    public void clearLinkRequest(Long chatId) {
        linkRequests.remove(chatId);
    }
}
