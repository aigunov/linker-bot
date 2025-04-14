package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.config.MigrationsRunner;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.chat.SqlChatRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({
    SqlChatRepository.class,
    MigrationsRunner.class,
    SqlTagRepository.class,
})
@Testcontainers
@TestPropertySource(properties = "app.db.access-type=sql")
public class SqlTagRepositoryTest {

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

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);
    }

    private Chat chat;

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

        chat = Chat.builder().tgId(123L).nickname("user1").build();
    }

    @Test
    @Transactional
    void saveTag_shouldPersistTag() {
        chat = chatRepository.save(chat);
        Tag tag =
                Tag.builder().chat(chat).tag("test_tag").links(new HashSet<>()).build();

        tag = tagRepository.save(tag);
        assertThat(tag.id()).isNotNull();

        var retrievedOpt = tagRepository.findById(tag.id());
        assertThat(retrievedOpt).isPresent();
        var retrieve = retrievedOpt.get();
        assertThat(retrieve.tag()).isEqualTo(tag.tag());
        assertThat(retrieve.id()).isEqualTo(tag.id());
    }

    @Test
    @Transactional
    void saveTag_shouldUpsertTagInsteadOfCreatingNew() {
        chat = chatRepository.save(chat);
        Tag tag = Tag.builder()
                .chat(chat)
                .tag("test_tag")
                .links(new HashSet<>())
                .build();

        tag = tagRepository.save(tag);
        var updatedTag = Tag.builder()
                .chat(chat)
                .tag("test_tag")
                .links(new HashSet<>())
                .build();

        updatedTag = tagRepository.save(tag);

        assertThat(tag.id()).isEqualTo(updatedTag.id());

        var tags = tagRepository.findAll();
        assertThat(tags).hasSize(1);
    }

    @Test
    @Transactional
    void saveAll_shouldUpsertExistingTagInsteadOfCreatingNew() {
        chat = chatRepository.save(chat);
        Tag tag1 = Tag.builder()
                .chat(chat)
                .tag("tag_1")
                .links(new HashSet<>())
                .build();

        tag1 = tagRepository.save(tag1);
        var tags = List.of(
                Tag.builder()
                        .chat(chat)
                        .tag("tag_1")
                        .links(new HashSet<>())
                        .build(),

                Tag.builder()
                        .chat(chat)
                        .tag("tag_2")
                        .links(new HashSet<>())
                        .build());
        tags = (List<Tag>) tagRepository.saveAll(tags);

        assertThat(tags).hasSize(2);
        assertThat(tag1.id()).isEqualTo(tags.getFirst().id());
        assertThat(tags.getLast().tag()).isEqualTo("tag_2");

    }

    @Test
    @Transactional
    void deleteById_shouldDeleteTagAndRelatedLinks() {
        chat = chatRepository.save(chat);
        Tag tag = Tag.builder()
                .chat(chat)
                .tag("delete_test")
                .links(new java.util.HashSet<>())
                .build();
        tag = tagRepository.save(tag);

        tagRepository.deleteById(tag.id());

        Optional<Tag> retrieved = tagRepository.findById(tag.id());
        assertThat(retrieved).isEmpty();
    }

    @Test
    @Transactional
    void findByTgIdAndTag_shouldReturnTag() {
        chat = chatRepository.save(chat);
        Tag tag = Tag.builder()
                .chat(chat)
                .tag("lookup_tag")
                .links(new java.util.HashSet<>())
                .build();
        tag = tagRepository.save(tag);

        Optional<Tag> found = tagRepository.findByTgIdAndTag(chat.tgId(), "lookup_tag");
        assertThat(found).isPresent();
        assertThat(found.get().tag()).isEqualTo("lookup_tag");
    }

    @Test
    @Transactional
    void findAllByChatIdAndNotInTagToLinkTable_shouldReturnEmptyWhenAssociationExists() {
        chat = chatRepository.save(chat);
        Tag tag = Tag.builder()
                .chat(chat)
                .tag("isolated_tag")
                .links(new java.util.HashSet<>())
                .build();
        tagRepository.save(tag);

        List<Tag> results = (List<Tag>) tagRepository.findAllByChatIdAndNotInTagToLinkTable(chat.id());
        assertThat(results).hasSize(1);
    }

    @Test
    @Transactional
    void deleteAllTags_shouldDeleteAll() {
        chat = chatRepository.save(chat);
        var tag1 = Tag.builder().chat(chat).tag("t1").links(new HashSet<>()).build();
        var tag2 = Tag.builder().chat(chat).tag("t2").links(new HashSet<>()).build();
        tagRepository.save(tag1);
        tagRepository.save(tag2);

        tagRepository.deleteAll();

        List<Tag> results = (List<Tag>) tagRepository.findAll();
        assertThat(results).isEmpty();
    }
}
