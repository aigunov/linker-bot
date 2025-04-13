package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
import java.util.HashSet;
import java.util.List;
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
public class OrmFilterRepositoryTest {

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
        chat = chatRepository.save(Chat.builder()
            .tgId(123L)
            .nickname("orm-user")
            .build());
    }

    @Test
    @Transactional
    void saveFilter_shouldPersist() {
        Filter filter = Filter.builder().chat(chat).parameter("key").value("val").links(new HashSet<>()).build();
        filter = filterRepository.save(filter);

        assertThat(filter.id()).isNotNull();
        var found = filterRepository.findById(filter.id());
        assertThat(found).isPresent();
        assertThat(found.get().parameter()).isEqualTo("key");
        assertThat(found.get().value()).isEqualTo("val");
    }

    @Test
    @Transactional
    void deleteById_shouldRemoveFilter() {
        Filter filter = filterRepository.save(Filter.builder()
            .chat(chat)
            .parameter("del")
            .value("me")
            .links(new HashSet<>())
            .build());

        filterRepository.deleteById(filter.id());

        assertThat(filterRepository.findById(filter.id())).isEmpty();
    }

    @Test
    @Transactional
    void findByTgIdAndFilter_shouldFindCorrectFilter() {
        Filter filter = filterRepository.save(Filter.builder()
            .chat(chat)
            .parameter("lang")
            .value("java")
            .links(new HashSet<>())
            .build());

        var found = filterRepository.findByTgIdAndFilter(chat.tgId(), "lang", "java");

        assertThat(found).isPresent();
        assertThat(found.get().parameter()).isEqualTo("lang");
        assertThat(found.get().value()).isEqualTo("java");
    }

    @Test
    @Transactional
    void findAllByChatIdAndNotInLinkToFilterTable_shouldReturnFilterIfNotLinked() {
        Filter filter = filterRepository.save(Filter.builder()
            .chat(chat)
            .parameter("only")
            .value("me")
            .links(new HashSet<>())
            .build());

        var result = (List<Filter>) filterRepository.findAllByChatIdAndNotInLinkToFilterTable(chat.id());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).parameter()).isEqualTo("only");
        assertThat(result.get(0).value()).isEqualTo("me");
    }

    @Test
    @Transactional
    void deleteAll_shouldDeleteAllFilters() {
        filterRepository.save(Filter.builder().chat(chat).parameter("p1").value("v1").links(new HashSet<>()).build());
        filterRepository.save(Filter.builder().chat(chat).parameter("p2").value("v2").links(new HashSet<>()).build());

        filterRepository.deleteAll();

        assertThat(filterRepository.findAll()).isEmpty();
    }
}
