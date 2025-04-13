package backend.academy.bot.service;

import dto.GetLinksRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ListRequestService {
    private final Map<Long, GetLinksRequest> listRequests = new ConcurrentHashMap<>();

    public void createListRequest(final Long chatId, final String tags) {
        var request = GetLinksRequest.builder().tags(List.of(tags.split(" "))).build();
        listRequests.put(chatId, request);
    }

    public void updateListRequestTags(Long chatId, String tags) {
        listRequests.computeIfPresent(chatId, (k, v) -> {
            v.tags().addAll(List.of(tags.split(" ")));
            return v;
        });
    }

    public GetLinksRequest getListRequest(Long chatId) {
        return listRequests.get(chatId);
    }

    public void clearLinkRequest(Long chatId) {
        listRequests.remove(chatId);
    }
}
