package backend.academy.scrapper.repository.chat;

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
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Testcontainers
@TestPropertySource(properties = {
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


//    @Test
//    @Transactional
//    void deleteByTgId_shouldDeleteChatAndRelations() {
//        // Сохраняем чат
//        Chat savedChat = chatRepository.save(chat1);
//        Long tgId = savedChat.tgId();
//
//        // Сохраняем Tag и Filter
//        Tag tag = Tag.builder()
//            .chat(savedChat)
//            .tag("tag")
//            .build();
//        tag = tagRepository.save(tag);
//
//        Filter filter = Filter.builder()
//            .chat(savedChat)
//            .parameter("p")
//            .value("v")
//            .build();
//        filter = filterRepository.save(filter);
//
//        // Создаем и сохраняем Link, связываем с ранее сохраненными сущностями
//        Link link = Link.builder()
//            .url("https://example.com")
//            .lastUpdate(LocalDateTime.now())
//            .chats(Set.of(savedChat))
//            .tags(Set.of(tag))
//            .filters(Set.of(filter))
//            .build();
//
//        // Связываем Tag и Filter обратно с Link — для двунаправленных связей
//        tag.links(Set.of(link));
//        filter.links(Set.of(link));
//
//        linkRepository.save(link);
//
//        // Выполняем удаление чата по tgId
//        chatRepository.deleteByTgId(tgId);
//
//        // Проверяем что все связанные данные тоже удалены
//        assertThat(chatRepository.findByTgId(tgId)).isEmpty();
//        assertThat(tagRepository.findAll()).isEmpty();
//        assertThat(filterRepository.findAll()).isEmpty();
//        assertThat(linkRepository.findAll()).isEmpty(); // <-- если нужно проверить и это
//    }

