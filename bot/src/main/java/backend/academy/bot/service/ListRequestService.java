package backend.academy.bot.service;

import dto.GetLinksRequest;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ListRequestService {
    private final Map<Long, GetLinksRequest> listRequests = new ConcurrentHashMap<>();

    public void createListRequest(Long chatId){
        var request = GetLinksRequest.builder().build();
        listRequests.put(chatId, request);
    }


    public void updateListRequestTags(Long chatId, String tags) {
        listRequests.computeIfPresent(chatId, (k, v) -> {
            v.tags().addAll(List.of(tags.split(" ")));
            return v;
        });
    }

    public void clearLinkRequest(Long chatId) {
        listRequests.remove(chatId);
    }
}
