package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import backend.academy.scrapper.client.RestNotificationClient;
import backend.academy.scrapper.client.UpdateCheckingClient;
import backend.academy.scrapper.config.MigrationsRunner;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(
        properties = {
            "app.scrapper.page-size=10",
            "app.scrapper.threads-count=1",
            "app.scrapper.scheduled-time=100000",
            "app.db.access-type=orm"
        })
public class ScrapperServiceTest {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MigrationsRunner migrationsRunner;

    @MockitoBean
    private UpdateCheckingClient gitHubClient;

    @MockitoBean
    private UpdateCheckingClient stackOverflowClient;

    @MockitoBean
    private RestNotificationClient restNotificationClient;

    @Autowired
    private ScrapperService scrapperService;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("app.scrapper.page-size", () -> 10);
        registry.add("app.scrapper.threads-count", () -> 1);
        registry.add("app.scrapper.scheduled-time", () -> 100000);
        registry.add("app.db.access-type", () -> "orm");
    }

    @BeforeEach
    @Transactional
    void setup() {
        var instant = LocalDateTime.now().minusDays(2);

        migrationsRunner.runMigrations();

        linkRepository.deleteAll();

        Chat chat = Chat.builder().tgId(123L).nickname("mr-white").build();

        chat = chatRepository.save(chat);

        Link githubLink = Link.builder()
                .url("https://github.com/test/repo")
                .lastUpdate(instant)
                .chats(Set.of(chat))
                .build();

        Link soLink = Link.builder()
                .url("https://stackoverflow.com/questions/123456/test")
                .lastUpdate(instant)
                .chats(Set.of(chat))
                .build();

        linkRepository.saveAll(List.of(githubLink, soLink));

        Mockito.when(gitHubClient.checkUpdates(any()))
                .thenReturn(Optional.of(
                        UpdateInfo.builder().date(LocalDateTime.now()).build()));

        Mockito.when(stackOverflowClient.checkUpdates(any()))
                .thenReturn(Optional.of(
                        UpdateInfo.builder().date(LocalDateTime.now()).build()));
    }

    @Test
    void testScrapperShouldUpdateLinksAndSendNotification() {
        scrapperService.scrapper();

        var updatedLinks = StreamSupport.stream(linkRepository.findAll().spliterator(), false)
                .toList();
        assertThat(updatedLinks)
                .allMatch(link -> link.lastUpdate().isAfter(LocalDateTime.now().minusMinutes(1)));

        Mockito.verify(restNotificationClient, Mockito.atLeastOnce())
                .sendLinkUpdate(Mockito.argThat(update ->
                        update.url().contains("github.com") || update.url().contains("stackoverflow.com")));
    }
}
