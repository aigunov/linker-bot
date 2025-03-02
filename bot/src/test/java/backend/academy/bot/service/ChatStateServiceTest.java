package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.state.ChatState;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatStateServiceTest {

    private ChatStateService chatStateService;
    private final String chatId = "12345";

    @BeforeEach
    void setUp() {
        chatStateService = new ChatStateService();
    }

    @Test
    void shouldSetChatStateAndRetrieveIt() {
        chatStateService.setChatState(chatId, ChatState.MENU);
        assertEquals(ChatState.MENU, chatStateService.getLastChatState(chatId));
    }

    @Test
    void shouldReturnNullWhenGettingLastStateFromEmptyStack() {
        assertNull(chatStateService.getLastChatState(chatId));
    }

    @Test
    void shouldReturnEmptyOptionalWhenPeekingEmptyStack() {
        assertTrue(chatStateService.peekLastChatState(chatId).isEmpty());
    }

    @Test
    void shouldPeekLastChatStateWithoutRemovingIt() {
        chatStateService.setChatState(chatId, ChatState.MENU);
        Optional<ChatState> lastState = chatStateService.peekLastChatState(chatId);

        assertTrue(lastState.isPresent());
        assertEquals(ChatState.MENU, lastState.get());

        assertEquals(ChatState.MENU, chatStateService.getLastChatState(chatId));
    }

    @Test
    void shouldMaintainStackSizeLimit() {
        for (int i = 0; i < 15; i++) {
            chatStateService.setChatState(chatId, ChatState.MENU);
        }

        List<ChatState> states = chatStateService.getAllChatStates(chatId);
        assertEquals(10, states.size());
    }

    @Test
    void shouldReturnAllChatStates() {
        chatStateService.setChatState(chatId, ChatState.MENU);
        chatStateService.setChatState(chatId, ChatState.TRACK);
        chatStateService.setChatState(chatId, ChatState.LIST);

        List<ChatState> states = chatStateService.getAllChatStates(chatId);
        assertEquals(3, states.size());
        assertEquals(List.of(ChatState.MENU, ChatState.TRACK, ChatState.LIST), states);
    }
}
