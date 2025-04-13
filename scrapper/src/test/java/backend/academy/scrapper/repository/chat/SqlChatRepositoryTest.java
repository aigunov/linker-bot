package backend.academy.scrapper.repository.chat;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.config.MigrationsRunner;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.repository.filter.FilterRepository;
import backend.academy.scrapper.repository.filter.SqlFilterRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.link.SqlLinkRepository;
import backend.academy.scrapper.repository.tag.SqlTagRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import java.time.LocalDateTime;
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
public class SqlChatRepositoryTest {

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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    }

    private Chat chat1;
    private Chat chat2;

    @BeforeEach
    void setUp() {
        migrationsRunner.runMigrations();

        // Clean up tables before each test
        jdbcTemplate.execute("DELETE FROM link_to_chat");
        jdbcTemplate.execute("DELETE FROM tag_to_link");
        jdbcTemplate.execute("DELETE FROM link_to_filter");
        jdbcTemplate.execute("DELETE FROM tag");
        jdbcTemplate.execute("DELETE FROM filter");
        jdbcTemplate.execute("DELETE FROM link");
        jdbcTemplate.execute("DELETE FROM chat");

        chat1 = Chat.builder().tgId(123L).nickname("user1").build();
        chat2 = Chat.builder().tgId(456L).nickname("user2").build();
    }

    @Test
    void saveChat() {
        Chat savedChat = chatRepository.save(chat1);
        assertThat(savedChat.id()).isNotNull();
        Optional<Chat> retrievedChatOpt = chatRepository.findById(savedChat.id());
        assertThat(retrievedChatOpt).isPresent();
        var retrievedChat = retrievedChatOpt.get();
        assertThat(retrievedChat.id()).isEqualTo(savedChat.id());
    }

    @Test
    void deleteById() {
        Chat savedChat = chatRepository.save(chat1);
        UUID chatId = savedChat.id();

        // Create related entities
        Tag tag = Tag.builder().chat(savedChat).tag("test_tag").build();
        Filter filter =
                Filter.builder().chat(savedChat).parameter("param").value("val").build();
        Link link = Link.builder()
                .url("http://example.com")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(savedChat))
                .tags(Set.of(tag))
                .filters(Set.of(filter))
                .build();

        tag = tagRepository.save(tag);
        filter = filterRepository.save(filter);
        link = linkRepository.save(link);

        chatRepository.deleteById(chatId);
        assertThat(chatRepository.findById(chatId)).isEmpty();

        // Verify related entities are deleted
        assertThat(jdbcTemplate.queryForList("SELECT * FROM link_to_chat WHERE chat_id = ?", UUID.class, chatId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM tag WHERE chat_id = ?", UUID.class, chatId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM filter WHERE chat_id = ?", UUID.class, chatId))
                .isEmpty();
    }

    @Test
    void deleteByTgId() {
        Chat savedChat = chatRepository.save(chat1);
        Long tgId = savedChat.tgId();
        UUID chatId = savedChat.id();

        Tag tag = Tag.builder().chat(savedChat).tag("test_tag").build();
        Filter filter =
                Filter.builder().chat(savedChat).parameter("param").value("val").build();
        Link link = Link.builder()
                .url("http://example.com")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(savedChat))
                .tags(Set.of(tag))
                .filters(Set.of(filter))
                .build();

        tag = tagRepository.save(tag);
        filter = filterRepository.save(filter);
        link = linkRepository.save(link);

        chatRepository.deleteByTgId(tgId);
        assertThat(chatRepository.findByTgId(tgId)).isEmpty();

        // Verify related entities are deleted
        assertThat(jdbcTemplate.queryForList("SELECT * FROM link_to_chat WHERE chat_id = ?", UUID.class, chatId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM tag WHERE chat_id = ?", UUID.class, chatId))
                .isEmpty();
        assertThat(jdbcTemplate.queryForList("SELECT * FROM filter WHERE chat_id = ?", UUID.class, chatId))
                .isEmpty();
    }

    @Test
    void findById() {
        var savedChat = chatRepository.save(chat1);
        var savedChatOpt = chatRepository.findById(savedChat.id());
        assertThat(savedChatOpt).isPresent();
        var retrieveChat = savedChatOpt.get();
        assertThat(retrieveChat.id()).isEqualTo(savedChat.id());
    }

    @Test
    void findByTgId() {
        var savedChat = chatRepository.save(chat1);
        var foundChatOpt = chatRepository.findByTgId(savedChat.tgId());
        assertThat(foundChatOpt).isPresent();
        assertThat(foundChatOpt.get().tgId()).isEqualTo(savedChat.tgId());
    }

    @Test
    void findAll() {
        chatRepository.save(chat1);
        chatRepository.save(chat2);
        List<Chat> allChats = (List<Chat>) chatRepository.findAll();
        assertThat(allChats).hasSize(2);
    }
}
