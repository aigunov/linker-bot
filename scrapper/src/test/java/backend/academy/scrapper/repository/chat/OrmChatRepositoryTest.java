package backend.academy.scrapper.repository.chat;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.repository.filter.FilterRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
public class OrmChatRepositoryTest {

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

    private Chat chat1;
    private Chat chat2;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
        filterRepository.deleteAll();
        linkRepository.deleteAll();
        chatRepository.deleteAll();

        chat1 = Chat.builder().tgId(123L).nickname("user1").build();
        chat2 = Chat.builder().tgId(456L).nickname("user2").build();
    }

    @Transactional
    @Test
    void saveChat() {
        Chat saved = chatRepository.save(chat1);
        assertThat(saved.id()).isNotNull();

        Optional<Chat> found = chatRepository.findById(saved.id());
        assertThat(found).isPresent();
        assertThat(found.get().tgId()).isEqualTo(chat1.tgId());
    }

    @Test
    @Transactional
    void deleteById_cascadesCorrectly() {
        Chat savedChat = chatRepository.save(chat1);
        UUID chatId = savedChat.id();

        chatRepository.deleteById(chatId);

        assertThat(chatRepository.findById(chatId)).isEmpty();
    }

    @Test
    void findAll_shouldReturnAllChats() {
        chatRepository.saveAll(List.of(chat1, chat2));
        var all = chatRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void findByTgId_shouldReturnCorrectChat() {
        var saved = chatRepository.save(chat1);
        var found = chatRepository.findByTgId(saved.tgId());

        assertThat(found).isPresent();
        assertThat(found.get().nickname()).isEqualTo("user1");
    }
}
