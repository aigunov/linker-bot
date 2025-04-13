package backend.academy.scrapper.repository.link;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.config.MigrationsRunner;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.chat.SqlChatRepository;
import backend.academy.scrapper.repository.filter.FilterRepository;
import backend.academy.scrapper.repository.filter.SqlFilterRepository;
import backend.academy.scrapper.repository.tag.SqlTagRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@JdbcTest
@Import({
    SqlChatRepository.class,
    MigrationsRunner.class,
    SqlLinkRepository.class,
    SqlTagRepository.class,
    SqlFilterRepository.class
})
@Testcontainers
@TestPropertySource(properties = "app.db.access-type=sql")
public class SqlLinkRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private MigrationsRunner migrationsRunner;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private FilterRepository filterRepository;

    private Chat chat;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    }

    @BeforeEach
    void setUp() {
        migrationsRunner.runMigrations();

        jdbcTemplate.execute("DELETE FROM link_to_chat");
        jdbcTemplate.execute("DELETE FROM tag_to_link");
        jdbcTemplate.execute("DELETE FROM link_to_filter");
        jdbcTemplate.execute("DELETE FROM tag");
        jdbcTemplate.execute("DELETE FROM filter");
        jdbcTemplate.execute("DELETE FROM link");
        jdbcTemplate.execute("DELETE FROM chat");

        chat = chatRepository.save(
                Chat.builder().tgId(100L).nickname("link_user").build());
    }

    @Test
    @Transactional
    void saveLink_shouldPersistCorrectly() {
        Tag tag = tagRepository.save(
                Tag.builder().chat(chat).tag("spring").links(new HashSet<>()).build());
        Filter filter = filterRepository.save(Filter.builder()
                .chat(chat)
                .parameter("lang")
                .value("java")
                .links(new HashSet<>())
                .build());

        Link link = Link.builder()
                .url("https://spring.io")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(chat))
                .tags(Set.of(tag))
                .filters(Set.of(filter))
                .build();

        Link savedLink = linkRepository.save(link);
        assertThat(savedLink.id()).isNotNull();

        Optional<Link> found = linkRepository.findById(savedLink.id());
        assertThat(found).isPresent();
        assertThat(found.get().url()).isEqualTo("https://spring.io");
    }

    @Test
    @Transactional
    void deleteLink_shouldRemoveAllAssociations() {
        Tag tag = tagRepository.save(
                Tag.builder().chat(chat).tag("delete").links(new HashSet<>()).build());
        Filter filter = filterRepository.save(Filter.builder()
                .chat(chat)
                .parameter("env")
                .value("prod")
                .links(new HashSet<>())
                .build());

        Link link = Link.builder()
                .url("https://delete.me")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(chat))
                .tags(Set.of(tag))
                .filters(Set.of(filter))
                .build();

        Link saved = linkRepository.save(link);
        linkRepository.deleteById(saved.id());

        assertThat(linkRepository.findById(saved.id())).isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM tag_to_link WHERE link_id = ?", UUID.class, saved.id()))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM link_to_chat WHERE link_id = ?", UUID.class, saved.id()))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM link_to_filter WHERE link_id = ?", UUID.class, saved.id()))
                .isEmpty();
    }

    @Test
    @Transactional
    void findByTgIdAndUrl_shouldReturnExpectedLink() {
        Tag tag = tagRepository.save(
                Tag.builder().chat(chat).tag("findme").links(new HashSet<>()).build());
        Filter filter = filterRepository.save(Filter.builder()
                .chat(chat)
                .parameter("q")
                .value("v")
                .links(new HashSet<>())
                .build());

        String url = "https://findme.com";

        Link link = Link.builder()
                .url(url)
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(chat))
                .tags(Set.of(tag))
                .filters(Set.of(filter))
                .build();

        linkRepository.save(link);
        Optional<Link> found = linkRepository.findByTgIdAndUrl(chat.tgId(), url);

        assertThat(found).isPresent();
        assertThat(found.get().url()).isEqualTo(url);
    }

    @Test
    @Transactional
    void findAllByTgId_shouldReturnAllLinks() {
        for (int i = 0; i < 3; i++) {
            Link link = Link.builder()
                    .url("https://example" + i + ".com")
                    .lastUpdate(LocalDateTime.now())
                    .chats(Set.of(chat))
                    .tags(Set.of())
                    .filters(Set.of())
                    .build();
            linkRepository.save(link);
        }

        List<Link> links = (List<Link>) linkRepository.findAllByTgId(chat.tgId());
        assertThat(links).hasSize(3);
    }

    @Test
    @Transactional
    void findLinksByTgIdAndTags_shouldReturnFilteredLinks() {
        Tag springTag = tagRepository.save(
                Tag.builder().chat(chat).tag("spring").links(new HashSet<>()).build());
        Tag otherTag = tagRepository.save(
                Tag.builder().chat(chat).tag("other").links(new HashSet<>()).build());

        Link l1 = Link.builder()
                .url("https://spring.dev")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(chat))
                .tags(Set.of(springTag))
                .filters(Set.of())
                .build();

        Link l2 = Link.builder()
                .url("https://other.com")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(chat))
                .tags(Set.of(otherTag))
                .filters(Set.of())
                .build();

        linkRepository.save(l1);
        linkRepository.save(l2);

        List<Link> result = (List<Link>) linkRepository.findLinksByTgIdAndTags(chat.tgId(), List.of("spring"));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).url()).isEqualTo("https://spring.dev");
    }
}
