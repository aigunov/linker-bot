package backend.academy.bot.service;

import backend.academy.bot.state.ChatState;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class ChatStateService {
    private static final int MAX_STACK_SIZE = 10;
    private final Map<String, Deque<ChatState>> chatStates = new ConcurrentHashMap<>();

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

    public Optional<ChatState> peekLastChatState(String chatId) {
        Deque<ChatState> stateStack = chatStates.get(chatId);
        if (stateStack == null || stateStack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(stateStack.peekLast());
    }

    public List<ChatState> getAllChatStates(String chatId) {
        return chatStates.getOrDefault(chatId, new LinkedList<>()).stream().toList();
    }
}
