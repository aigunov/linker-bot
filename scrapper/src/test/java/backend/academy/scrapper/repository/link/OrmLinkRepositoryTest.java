package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.filter.FilterRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource(properties = {
    "app.db.access-type=orm",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.hibernate.ddl-auto=create"
})
public class OrmLinkRepositoryTest {

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
    void setUp() {
        tagRepository.deleteAll();
        filterRepository.deleteAll();
        linkRepository.deleteAll();
        chatRepository.deleteAll();

        chat = chatRepository.save(Chat.builder().tgId(123L).nickname("user1").build());
    }


    @Test
    @Transactional
    void saveLink_shouldPersistCorrectly() {
        Tag tag = tagRepository.save(Tag.builder().chat(chat).tag("tag1").build());
        Filter filter = filterRepository.save(Filter.builder().chat(chat).parameter("param").value("val").build());

        Link link = Link.builder()
            .url("https://example.org")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .tags(Set.of(tag))
            .filters(Set.of(filter))
            .build();

        Link saved = linkRepository.save(link);

        assertThat(saved.id()).isNotNull();
        assertThat(linkRepository.findById(saved.id())).isPresent();
    }

    @Test
    @Transactional
    void findByTgIdAndUrl_shouldReturnCorrectLink() {
        Link link = Link.builder()
            .url("https://find-me.com")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .build();

        linkRepository.save(link);

        Optional<Link> found = linkRepository.findByTgIdAndUrl(chat.tgId(), "https://find-me.com");
        assertThat(found).isPresent();
        assertThat(found.get().url()).isEqualTo("https://find-me.com");
    }

    @Test
    @Transactional
    void findByUrl_shouldReturnLink() {
        Link link = Link.builder()
            .url("https://lookup.com")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .build();

        linkRepository.save(link);

        Optional<Link> found = linkRepository.findByUrl("https://lookup.com");
        assertThat(found).isPresent();
        assertThat(found.get().url()).isEqualTo("https://lookup.com");
    }

    @Test
    @Transactional
    void findAllByTgId_shouldReturnAllLinksOfChat() {
        for (int i = 1; i <= 3; i++) {
            linkRepository.save(Link.builder()
                .url("https://site" + i + ".com")
                .lastUpdate(LocalDateTime.now())
                .chats(Set.of(chat))
                .build());
        }

        List<Link> links = (List<Link>) linkRepository.findAllByTgId(chat.tgId());
        assertThat(links).hasSize(3);
    }

    @Test
    @Transactional
    void findLinksByTgIdAndTags_shouldReturnMatchingLinks() {
        Tag tag1 = tagRepository.save(Tag.builder().links(new HashSet<>()).chat(chat).tag("java").build());
        Tag tag2 = tagRepository.save(Tag.builder().links(new HashSet<>()).chat(chat).tag("spring").build());

        Link javaLink = linkRepository.save(Link.builder()
            .url("https://java.dev")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .tags(Set.of(tag1))
            .build());

        Link springLink = linkRepository.save(Link.builder()
            .url("https://spring.io")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .tags(Set.of(tag2))
            .build());

        List<Link> result = (List<Link>) linkRepository.findLinksByTgIdAndTags(chat.tgId(), List.of("spring"));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().url()).isEqualTo("https://spring.io");
    }

    @Test
    @Transactional
    void findAllWithChats_shouldIncludeChatsInEntityGraph() {
        Link link = linkRepository.save(Link.builder()
            .url("https://linked.com")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .build());

        List<Link> result = (List<Link>) linkRepository.findAllWithChats(PageRequest.of(0, 10));
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).chats()).isNotEmpty();
    }
}
