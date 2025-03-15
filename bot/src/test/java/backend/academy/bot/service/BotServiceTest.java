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
import dto.ListLinkResponse;
import dto.RemoveLinkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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

    @BeforeEach
    void setUp() {
        // arrange
        addLinkRequestService = new AddLinkRequestService();
        botService = new BotService(client, addLinkRequestService);
        botService.setTelegramBot(telegramBot);
    }

    @Test
    void getTrackingLinks_shouldCallClientAndReturnBody() {
        // arrange
        Long chatId = 123L;
        ListLinkResponse expectedResponse = ListLinkResponse.builder().build();
        when(client.getAllTrackedLinks(chatId)).thenReturn(ResponseEntity.ok(expectedResponse));

        // act
        Object result = botService.getTrackingLinks(chatId);

        // assert
        assertThat(result).isEqualTo(expectedResponse);
        verify(client).getAllTrackedLinks(chatId);
    }

    @Test
    void chatRegistration_shouldCallClientAndReturnBody() {
        // arrange
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

        // act
        Object result = botService.chatRegistration(update);

        // assert
        assertThat(result).isEqualTo("Registered");
        verify(client).registerChat(any());
    }

    @Test
    void commitLinkTracking_shouldThrowExceptionOnClientError() {
        // arrange
        Long chatId = 123L;
        addLinkRequestService.createLinkRequest(chatId, "https://example.com");
        ResponseEntity<Object> badRequestResponse = ResponseEntity.badRequest().body("Bad Request");
        when(client.addTrackedLink(any(), any())).thenReturn(badRequestResponse);

        // act
        var result = botService.commitLinkTracking(chatId);

        // assert
        assertThat(result).isEqualTo("Bad Request");
    }

    @Test
    void commitLinkUntrack_shouldCallClientAndReturnBody() {
        // arrange
        Long chatId = 123L;
        String message = "https://example.com";
        RemoveLinkRequest request = new RemoveLinkRequest(message);
        when(client.removeTrackedLink(chatId, request))
            .thenReturn(ResponseEntity.ok(LinkResponse.builder().build()));

        // act
        Object result = botService.commitLinkUntrack(chatId, message);

        // assert
        assertThat(result).isInstanceOf(LinkResponse.class);
        verify(client).removeTrackedLink(chatId, request);
    }

    @Test
    void commitLinkUntrack_shouldReturnBadRequestOnClientError() {
        // arrange
        Long chatId = 123L;
        String message = "https://example.com";
        RemoveLinkRequest request = new RemoveLinkRequest(message);
        ResponseEntity<Object> badRequestResponse = ResponseEntity.badRequest().body("Bad Request");
        when(client.removeTrackedLink(chatId, request)).thenReturn(badRequestResponse);

        // act
        Object result = botService.commitLinkUntrack(chatId, message);

        // assert
        assertThat(result).isEqualTo("Bad Request");
        verify(client).removeTrackedLink(chatId, request);
    }
}
