package backend.academy.scrapper.service;

import backend.academy.scrapper.client.NotificationClient;
import backend.academy.scrapper.client.UpdateCheckingClient;
import backend.academy.scrapper.config.MigrationsRunner;
import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.repository.link.LinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
@Testcontainers
public class ScrapperServiceTest {
    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
        .withDatabaseName("scrapper_db")
        .withUsername("aigunov")
        .withPassword("12345");

    @Autowired
    private LinkRepository linkRepository;

    @Mock
    private UpdateCheckingClient gitHubClient;

    @Mock
    private UpdateCheckingClient stackOverflowClient;

    @Mock
    private NotificationClient notificationClient;

    @Autowired
    private MigrationsRunner migrationsRunner;

    @Autowired
    @InjectMocks
    private ScrapperService scrapperService;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none"); // если используешь Liquibase
        registry.add("app.scrapper.page-size", () -> 10);
        registry.add("app.scrapper.threads-count", () -> 1);
        registry.add("app.scrapper.scheduled-time", () -> 100000);
        registry.add("app.db.access-type", () -> "orm");
    }

    @BeforeEach
    void setup() {

        migrationsRunner.runMigrations();


        linkRepository.deleteAll();

        Link githubLink = Link.builder()
            .url("https://github.com/test/repo")
            .lastUpdate(LocalDateTime.now().minusDays(2))
            .chats(Set.of(Chat.builder().tgId(123L).build()))
            .build();

        Link soLink = Link.builder()
            .url("https://stackoverflow.com/questions/123456/test")
            .lastUpdate(LocalDateTime.now().minusDays(2))
            .chats(Set.of(Chat.builder().tgId(456L).build()))
            .build();

        linkRepository.saveAll(List.of(githubLink, soLink));

        Mockito.when(gitHubClient.checkUpdates(any()))
            .thenReturn(Optional.of(UpdateInfo.builder().date(LocalDateTime.now()).build()));

        Mockito.when(stackOverflowClient.checkUpdates(any()))
            .thenReturn(Optional.of(UpdateInfo.builder().date(LocalDateTime.now()).build()));
    }

    @Test
    void testScrapperShouldUpdateLinksAndSendNotification() throws InterruptedException {
        scrapperService.scrapper();

        Thread.sleep(1000);

        var updatedLinks = StreamSupport.stream(linkRepository.findAll().spliterator(), false).toList();
        assertThat(updatedLinks).allMatch(link -> link.lastUpdate().isAfter(LocalDateTime.now().minusMinutes(1)));

        Mockito.verify(notificationClient, Mockito.atLeastOnce())
            .sendLinkUpdate(Mockito.argThat(update ->
                update.url().contains("github.com") || update.url().contains("stackoverflow.com")));
    }

}
