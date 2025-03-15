package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import backend.academy.scrapper.repository.InMemoryChatRepository;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RegisterChatRequest;
import dto.RemoveLinkRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScrapperServiceTest {

    @Mock
    private Mapper mapper;

    @Mock
    private InMemoryChatRepository chatRepository;

    @Mock
    private InMemoryLinkRepository linkRepository;

    @InjectMocks
    private ScrapperService scrapperService;

    private Chat chat;
    private Link link;
    private final Long chatId = 123L;
    private final UUID chatUUID = UUID.randomUUID();
    private final String testUrl = "https://github.com/test/repo";

    @BeforeEach
    void setUp() {
        // arrange
        chat = Chat.builder()
            .id(chatUUID)
            .chatId(chatId)
            .username("user1")
            .creationDate(LocalDateTime.now())
            .build();

        link = Link.builder()
            .id(UUID.randomUUID())
            .chatId(chatUUID)
            .url(testUrl)
            .tags(List.of("java", "spring"))
            .filters(List.of("open", "bug"))
            .lastUpdate(LocalDateTime.now())
            .build();
    }

    @Test
    void registerChat_ShouldRegisterChat_WhenChatDoesNotExist() {
        // arrange
        RegisterChatRequest request =
            RegisterChatRequest.builder().chatId(chatId).name("user1").build();

        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.empty());
        when(mapper.chatDtoToEntity(request)).thenReturn(chat);
        when(chatRepository.save(chat)).thenReturn(chat);

        // act
        String response = scrapperService.registerChat(chatId, request);

        // assert
        assertEquals("Вы зарегистрированы", response);
        verify(chatRepository, times(1)).save(chat);
    }

    @Test
    void registerChat_ShouldThrowException_WhenChatAlreadyExists() {
        // arrange
        RegisterChatRequest request =
            RegisterChatRequest.builder().chatId(chatId).name("user1").build();

        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> scrapperService.registerChat(chatId, request));
        verify(chatRepository, never()).save(any());
    }

    @Test
    void deleteChat_ShouldDeleteChat_WhenChatExists() {
        // arrange
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(chatRepository.deleteById(chat.id())).thenReturn(chat);

        // act
        String response = scrapperService.deleteChat(chatId);

        // assert
        assertEquals("Чат Успешно удален", response);
        verify(chatRepository, times(1)).deleteById(chat.id());
    }

    @Test
    void deleteChat_ShouldThrowException_WhenChatDoesNotExist() {
        // arrange
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(NoSuchElementException.class, () -> scrapperService.deleteChat(chatId));
        verify(chatRepository, never()).deleteById(any());
    }

    @Test
    void getAllTrackedLinks_ShouldReturnLinks_WhenLinksExist() {
        // arrange
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.findAllByChatId(chat.id())).thenReturn(List.of(link));
        when(mapper.linkToLinkResponse(link))
            .thenReturn(LinkResponse.builder()
                .id(link.id())
                .url(link.url())
                .tags(link.tags())
                .filters(link.filters())
                .build());

        // act
        ListLinkResponse response = scrapperService.getAllTrackedLinks(chatId);

        // assert
        assertEquals(1, response.size());
        assertEquals(testUrl, response.linkResponses().get(0).url());
        assertEquals(List.of("java", "spring"), response.linkResponses().get(0).tags());
    }

    @Test
    void getAllTrackedLinks_ShouldThrowException_WhenNoLinksFound() {
        // arrange
        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.findAllByChatId(chat.id())).thenReturn(List.of());

        // act & assert
        assertThrows(NoSuchElementException.class, () -> scrapperService.getAllTrackedLinks(chatId));
    }

    @Test
    void addTrackedLink_ShouldAddLink_WhenNotAlreadyTracked() {
        // arrange
        AddLinkRequest request = AddLinkRequest.builder()
            .uri(testUrl)
            .tags(List.of("java"))
            .filters(List.of("open"))
            .build();

        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.findByChatIdAndLink(chat.id(), testUrl)).thenReturn(Optional.empty());
        when(mapper.linkRequestToLink(request, chat)).thenReturn(link);
        when(linkRepository.save(link)).thenReturn(link);
        when(mapper.linkToLinkResponse(link))
            .thenReturn(LinkResponse.builder()
                .id(link.id())
                .url(link.url())
                .tags(link.tags())
                .filters(link.filters())
                .build());

        // act
        LinkResponse response = scrapperService.addTrackedLink(chatId, request);

        // assert
        assertEquals(testUrl, response.url());
        verify(linkRepository, times(1)).save(link);
    }

    @Test
    void addTrackedLink_ShouldThrowException_WhenLinkAlreadyTracked() {
        // arrange
        AddLinkRequest request = AddLinkRequest.builder()
            .uri(testUrl)
            .tags(List.of("java"))
            .filters(List.of("open"))
            .build();

        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.findByChatIdAndLink(chat.id(), testUrl)).thenReturn(Optional.of(link));

        // act & assert
        assertThrows(NoSuchElementException.class, () -> scrapperService.addTrackedLink(chatId, request));
        verify(linkRepository, never()).save(any());
    }

    @Test
    void removeTrackedLink_ShouldRemoveLink_WhenExists() {
        // arrange
        RemoveLinkRequest request = new RemoveLinkRequest(testUrl);

        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.findByChatIdAndLink(chat.id(), testUrl)).thenReturn(Optional.of(link));
        when(linkRepository.deleteById(link.id())).thenReturn(link);
        when(mapper.linkToLinkResponse(link))
            .thenReturn(LinkResponse.builder()
                .id(link.id())
                .url(link.url())
                .tags(link.tags())
                .filters(link.filters())
                .build());

        // act
        LinkResponse response = scrapperService.removeTrackedLink(chatId, request);

        // assert
        assertEquals(testUrl, response.url());
        verify(linkRepository, times(1)).deleteById(link.id());
    }

    @Test
    void removeTrackedLink_ShouldThrowException_WhenLinkDoesNotExist() {
        // arrange
        RemoveLinkRequest request = new RemoveLinkRequest(testUrl);

        when(chatRepository.findByChatId(chatId)).thenReturn(Optional.of(chat));
        when(linkRepository.findByChatIdAndLink(chat.id(), testUrl)).thenReturn(Optional.empty());

        // act & assert
        assertThrows(NoSuchElementException.class, () -> scrapperService.removeTrackedLink(chatId, request));
        verify(linkRepository, never()).deleteById(any());
    }
}
