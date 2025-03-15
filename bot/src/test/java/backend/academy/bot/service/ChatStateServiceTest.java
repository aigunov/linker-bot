package backend.academy.bot.service;

import backend.academy.bot.state.ChatState;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatStateServiceTest {

    private final String chatId = "12345";
    private ChatStateService chatStateService;

    @BeforeEach
    void setUp() {
        // arrange
        chatStateService = new ChatStateService();
    }

    @Test
    void shouldSetChatStateAndRetrieveIt() {
        // act
        chatStateService.setChatState(chatId, ChatState.MENU);

        // assert
        assertEquals(ChatState.MENU, chatStateService.getLastChatState(chatId));
    }

    @Test
    void shouldReturnNullWhenGettingLastStateFromEmptyStack() {
        // act
        ChatState lastState = chatStateService.getLastChatState(chatId);

        // assert
        assertNull(lastState);
    }

    @Test
    void shouldReturnEmptyOptionalWhenPeekingEmptyStack() {
        // act
        Optional<ChatState> lastState = chatStateService.peekLastChatState(chatId);

        // assert
        assertTrue(lastState.isEmpty());
    }

    @Test
    void shouldPeekLastChatStateWithoutRemovingIt() {
        // arrange
        chatStateService.setChatState(chatId, ChatState.MENU);

        // act
        Optional<ChatState> lastState = chatStateService.peekLastChatState(chatId);

        // assert
        assertTrue(lastState.isPresent());
        assertEquals(ChatState.MENU, lastState.get());
        assertEquals(ChatState.MENU, chatStateService.getLastChatState(chatId));
    }

    @Test
    void shouldMaintainStackSizeLimit() {
        // arrange
        for (int i = 0; i < 15; i++) {
            chatStateService.setChatState(chatId, ChatState.MENU);
        }

        // act
        List<ChatState> states = chatStateService.getAllChatStates(chatId);

        // assert
        assertEquals(10, states.size());
    }

    @Test
    void shouldReturnAllChatStates() {
        // arrange
        chatStateService.setChatState(chatId, ChatState.MENU);
        chatStateService.setChatState(chatId, ChatState.TRACK);
        chatStateService.setChatState(chatId, ChatState.LIST);

        // act
        List<ChatState> states = chatStateService.getAllChatStates(chatId);

        // assert
        assertEquals(3, states.size());
        assertEquals(List.of(ChatState.MENU, ChatState.TRACK, ChatState.LIST), states);
    }
}
