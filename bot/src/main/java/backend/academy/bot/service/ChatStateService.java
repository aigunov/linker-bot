package backend.academy.bot.service;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.state.ChatState;
import org.springframework.stereotype.Service;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatStateService {
    private static final int MAX_STACK_SIZE = 10;
    private final Map<String, Deque<ChatState>> chatStates = new ConcurrentHashMap<>();
    private final Map<Long, AddLinkRequest> linkRequests = new ConcurrentHashMap<>();


    public void setChatState(String chatId, ChatState chatState) {
        chatStates.computeIfAbsent(chatId, k -> new LinkedList<>());

        Deque<ChatState> stateStack = chatStates.get(chatId);

        if (stateStack.size() >= MAX_STACK_SIZE) {
            stateStack.pollFirst();
        }

        stateStack.addLast(chatState);
    }

    public ChatState getLastChatState(String chatId) {
        Deque<ChatState> stateStack = chatStates.get(chatId);
        if (stateStack == null || stateStack.isEmpty()) {
            return null;
        }
        return stateStack.pollLast();
    }

    public ChatState peekLastChatState(String chatId) {
        Deque<ChatState> stateStack = chatStates.get(chatId);
        if (stateStack == null || stateStack.isEmpty()) {
            return null;
        }
        return stateStack.peekLast();
    }

    public List<ChatState> getAllChatStates(String chatId) {
        return chatStates.getOrDefault(chatId, new LinkedList<>()).stream().toList();
    }


//TODO: вынести управление состоянием добавления ссылки в отдельный сервис
    // Методы для управления AddLinkRequest по chatId
    public void createLinkRequest(Long chatId, String uri) {
        linkRequests.put(chatId, AddLinkRequest.builder().uri(uri).build());
    }

    public AddLinkRequest getLinkRequest(Long chatId) {
        return linkRequests.get(chatId);
    }

    public void updateLinkRequestTags(Long chatId, String tags) {
        linkRequests.computeIfPresent(chatId, (k, v) -> {
            v.tags(List.of(tags.split(" ")));
            return v;
        });
    }

    public void updateLinkRequestFilters(Long chatId, String filters) {
        linkRequests.computeIfPresent(chatId, (k, v) -> {
            v.filters(List.of(filters.split(" ")));
            return v;
        });
    }

    public void clearLinkRequest(Long chatId) {
        linkRequests.remove(chatId);
    }
}
