package backend.academy.scrapper.repository.tag;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.filter.FilterRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@Testcontainers
@TestPropertySource(
        properties = {
            "app.db.access-type=orm",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.hibernate.ddl-auto=create"
        })
public class OrmTagRepositoryTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private FilterRepository filterRepository;

    private Chat chat;

    @BeforeEach
    void setup() {
        chat = chatRepository.save(
                Chat.builder().tgId(123L).nickname("orm-user").build());
    }

    @Test
    @Transactional
    void saveTag_shouldPersist() {
        Tag tag =
                Tag.builder().chat(chat).tag("test_tag").links(new HashSet<>()).build();
        tag = tagRepository.save(tag);

        assertThat(tag.id()).isNotNull();
        var found = tagRepository.findById(tag.id());
        assertThat(found).isPresent();
        assertThat(found.get().tag()).isEqualTo("test_tag");
    }

    @Test
    @Transactional
    void deleteById_shouldRemoveTag() {
        Tag tag = tagRepository.save(
                Tag.builder().chat(chat).tag("delete_me").links(new HashSet<>()).build());
        tagRepository.deleteById(tag.id());

        assertThat(tagRepository.findById(tag.id())).isEmpty();
    }

    @Test
    @Transactional
    void findByTgIdAndTag_shouldFindCorrectTag() {
        Tag tag = tagRepository.save(Tag.builder()
                .chat(chat)
                .tag("search_tag")
                .links(new HashSet<>())
                .build());

        Optional<Tag> found = tagRepository.findByTgIdAndTag(chat.tgId(), "search_tag");

        assertThat(found).isPresent();
        assertThat(found.get().tag()).isEqualTo("search_tag");
    }

    @Test
    @Transactional
    void findAllByTgIdIdAndNotInTagToLinkTable_shouldReturnTagIfNotLinked() {
        Tag tag = tagRepository.save(Tag.builder()
                .chat(chat)
                .tag("unlinked_tag")
                .links(new HashSet<>())
                .build());

        List<Tag> result = (List<Tag>) tagRepository.findAllByChatIdAndNotInTagToLinkTable(chat.id());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).tag()).isEqualTo("unlinked_tag");
    }

    @Test
    @Transactional
    void deleteAll_shouldDeleteAllTags() {
        var tag1 = tagRepository.save(
                Tag.builder().chat(chat).tag("t1").links(new HashSet<>()).build());
        var tag2 = tagRepository.save(
                Tag.builder().chat(chat).tag("t2").links(new HashSet<>()).build());

        tagRepository.deleteAll();

        assertThat(tagRepository.findAll()).isEmpty();
    }
}
