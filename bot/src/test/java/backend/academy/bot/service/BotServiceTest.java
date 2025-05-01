package backend.academy.bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.clients.JsonToApiErrorResponse;
import backend.academy.bot.clients.ScrapperClient;
import backend.academy.bot.configs.TelegramBot;
import backend.academy.bot.exception.TelegramApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RemoveLinkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {

    @Mock
    private ScrapperClient client;

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JsonToApiErrorResponse convertJsonToApiErrorResponse;

    @InjectMocks
    private BotService botService;

    private AddLinkRequestService addLinkRequestService;
    private ListRequestService listRequestService;

    @BeforeEach
    void setUp() {
//        addLinkRequestService = new AddLinkRequestService();
        botService = new BotService(client, addLinkRequestService, listRequestService);
        botService.setTelegramBot(telegramBot);
    }

    @Test
    void chatRegistration_shouldCallClientAndReturnBody() {
        Update update = Mockito.mock(Update.class);
        Message message = Mockito.mock(Message.class);
        User user = Mockito.mock(User.class);
        Chat chat = Mockito.mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.from()).thenReturn(user);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(123L);
        when(user.username()).thenReturn("testUser");

        when(client.registerChat(any())).thenReturn(ResponseEntity.ok("Registered"));

        Object result = botService.chatRegistration(update);

        assertThat(result).isEqualTo("Registered");
        verify(client).registerChat(any());
    }

    @Test
    void commitLinkTracking_shouldThrowExceptionOnClientError() {
        Long chatId = 123L;
        addLinkRequestService.createLinkRequest(chatId, "https://example.com");
        AddLinkRequest linkRequest = addLinkRequestService.getLinkRequest(chatId);

        assertThrows(TelegramApiException.class, () -> botService.commitLinkTracking(chatId));
        verify(client).addTrackedLink(chatId, linkRequest);
    }

    @Test
    void commitLinkUntrack_shouldCallClientAndReturnBody() {
        Long chatId = 123L;
        String message = "https://example.com";
        RemoveLinkRequest request = new RemoveLinkRequest(message);
        when(client.removeTrackedLink(chatId, request))
                .thenReturn(ResponseEntity.ok(LinkResponse.builder().build()));

        Object result = botService.commitLinkUntrack(chatId, message);

        assertThat(result).isInstanceOf(LinkResponse.class);
        verify(client).removeTrackedLink(chatId, request);
    }

    @Test
    void commitLinkUntrack_shouldThrowExceptionOnClientError() {
        Long chatId = 123L;
        String message = "https://example.com";
        RemoveLinkRequest request = new RemoveLinkRequest(message);

        assertThrows(TelegramApiException.class, () -> botService.commitLinkUntrack(chatId, message));
        verify(client).removeTrackedLink(chatId, request);
    }
}
