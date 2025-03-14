package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.NotificationClient;
import backend.academy.scrapper.client.UpdateCheckingClient;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScrapperServiceComponentTest {

    @Mock
    private InMemoryChatRepository chatRepository;

    @Mock
    private InMemoryLinkRepository linkRepository;

    @Mock
    private UpdateCheckingClient gitHubClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private LinkToApiRequestConverter linkToApiRequestConverter;

    @InjectMocks
    private ScrapperService scrapperService;

    private final UUID linkId = UUID.randomUUID();
    private final UUID chatId = UUID.randomUUID();
    private final String githubUrl = "https://github.com/aigunov/backend-academy";
    private final LocalDateTime oldUpdate = LocalDateTime.now().minusDays(1);
    private final LocalDateTime newUpdate = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        when(linkToApiRequestConverter.isGithubUrl(githubUrl)).thenReturn(true);
    }

    @Test
    void shouldUpdateLinkAndNotify_WhenNewUpdateAvailableFromGitHub() {
        Link oldLink = Link.builder()
                .id(linkId)
                .chatId(chatId)
                .url(githubUrl)
                .lastUpdate(oldUpdate)
                .build();
        Chat chat = Chat.builder()
                .id(chatId)
                .tgId(123L)
                .nickname("user")
                .creationDate(LocalDateTime.now())
                .build();

        when(linkRepository.findAll()).thenReturn(List.of(oldLink));
        when(gitHubClient.checkUpdates(any())).thenReturn(Optional.of(newUpdate));
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        scrapperService.scrapper();

        verify(notificationClient, times(1)).sendLinkUpdate(any());
    }

    @Test
    void shouldNotUpdateOrNotify_WhenNoNewUpdatesFromGitHub() {
        Link oldLink = Link.builder()
                .id(linkId)
                .chatId(chatId)
                .url(githubUrl)
                .lastUpdate(oldUpdate)
                .build();

        when(linkRepository.findAll()).thenReturn(List.of(oldLink));

        scrapperService.scrapper();

        assertEquals(oldUpdate, oldLink.lastUpdate());
        verify(notificationClient, never()).sendLinkUpdate(any());
    }
}
