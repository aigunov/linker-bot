package backend.academy.bot.service;

import backend.academy.bot.state.ChatState;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatStateService {
    private final Map<String, ChatState> chatStates = new ConcurrentHashMap<>();

    public void setChatStates(String chatId, ChatState chatState) {
        chatStates.put(chatId, chatState);
    }

    public ChatState getChatState(String chatId) {
        return chatStates.get(chatId);
    }
}
