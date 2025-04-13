package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.config.MigrationsRunner;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.repository.chat.ChatRepository;
import backend.academy.scrapper.repository.chat.SqlChatRepository;
import backend.academy.scrapper.repository.link.LinkRepository;
import backend.academy.scrapper.repository.link.SqlLinkRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import backend.academy.scrapper.repository.tag.SqlTagRepository;
import backend.academy.scrapper.repository.tag.TagRepository;
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
import static org.assertj.core.api.Assertions.assertThat;


@JdbcTest
@Import({SqlChatRepository.class, MigrationsRunner.class, SqlLinkRepository.class, SqlTagRepository.class,
    SqlFilterRepository.class})
@Testcontainers
@TestPropertySource(properties = "app.db.access-type=sql")
public class SqlFilterRepositoryTest {

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
    void saveFilter_shouldPersistFilter() {
        chat = chatRepository.save(chat);
        Filter filter = Filter.builder()
            .chat(chat)
            .parameter("param1")
            .value("value1")
            .links(new HashSet<>())
            .build();

        Filter savedFilter = filterRepository.save(filter);
        assertThat(savedFilter.id()).isNotNull();

        Optional<Filter> retrievedOpt = filterRepository.findById(savedFilter.id());
        assertThat(retrievedOpt).isPresent();
        Filter retrievedFilter = retrievedOpt.get();
        assertThat(retrievedFilter.parameter()).isEqualTo("param1");
        assertThat(retrievedFilter.value()).isEqualTo("value1");
    }

    @Test
    @Transactional
    void deleteById_shouldDeleteFilterAndRemoveFromLinkToFilter() {
        chat = chatRepository.save(chat);
        Filter filter = Filter.builder()
            .chat(chat)
            .parameter("param_delete")
            .value("value_delete")
            .links(new HashSet<>())
            .build();
        filter = filterRepository.save(filter);

        Link link = Link.builder()
            .url("http://example.com/delete")
            .lastUpdate(LocalDateTime.now())
            .chats(Set.of(chat))
            .filters(Set.of(filter))
            .tags(new HashSet<>())
            .build();
        link = linkRepository.save(link);

        filterRepository.deleteById(filter.id());

        assertThat(filterRepository.findById(filter.id())).isEmpty();
        assertThat(jdbcTemplate
            .queryForList("SELECT filter_id FROM link_to_filter WHERE link_id = ?", UUID.class, link.id())).isEmpty();
    }

    @Test
    @Transactional
    void findById_shouldReturnCorrectFilter() {
        chat = chatRepository.save(chat);
        Filter filter = Filter.builder()
            .chat(chat)
            .parameter("param_find")
            .value("value_find")
            .links(new HashSet<>())
            .build();
        filter = filterRepository.save(filter);

        Optional<Filter> foundFilter = filterRepository.findById(filter.id());
        assertThat(foundFilter).isPresent();
        assertThat(foundFilter.get().parameter()).isEqualTo("param_find");
        assertThat(foundFilter.get().value()).isEqualTo("value_find");
    }

    @Test
    @Transactional
    void findByTgIdAndFilter_shouldReturnCorrectFilter() {
        chat = chatRepository.save(chat);
        Filter filter = Filter.builder()
            .chat(chat)
            .parameter("param_tgid")
            .value("value_tgid")
            .links(new HashSet<>())
            .build();
        filterRepository.save(filter);

        Optional<Filter> foundFilter = filterRepository.findByTgIdAndFilter(chat.tgId(), "param_tgid", "value_tgid");
        assertThat(foundFilter).isPresent();
        assertThat(foundFilter.get().parameter()).isEqualTo("param_tgid");
        assertThat(foundFilter.get().value()).isEqualTo("value_tgid");
    }

    @Test
    @Transactional
    void findAllByChatIdAndNotInLinkToFilterTable_shouldReturnFiltersNotInLinkToFilter() {
        chat = chatRepository.save(chat);
        Filter filter1 = Filter.builder().chat(chat).parameter("param_not_in").value("value_not_in").links(new HashSet<>()).build();
        Filter filter2 = Filter.builder().chat(chat).parameter("param_in").value("value_in").links(new HashSet<>()).build();
        filter1 = filterRepository.save(filter1);
        filter2 = filterRepository.save(filter2);

        Link link = Link.builder().url("http://example.com/notin").lastUpdate(LocalDateTime.now()).chats(Set.of(chat)).filters(Set.of(filter2)).tags(new HashSet<>()).build();
        linkRepository.save(link);

        List<Filter> result = (List<Filter>) filterRepository.findAllByChatIdAndNotInLinkToFilterTable(chat.id());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(filter1.id());
    }

    @Test
    @Transactional
    void deleteAllFilters_shouldDeleteAllFilters() {
        chat = chatRepository.save(chat);
        Filter filter1 = Filter.builder().chat(chat).parameter("param_delete_all1").value("value_delete_all1").links(new HashSet<>()).build();
        Filter filter2 = Filter.builder().chat(chat).parameter("param_delete_all2").value("value_delete_all2").links(new HashSet<>()).build();
        filterRepository.save(filter1);
        filterRepository.save(filter2);

        List<Filter> allFiltersBeforeDelete = (List<Filter>) filterRepository.findAll();
        assertThat(allFiltersBeforeDelete).hasSize(2);

        filterRepository.deleteAll();

        List<Filter> allFiltersAfterDelete = (List<Filter>) filterRepository.findAll();
        assertThat(allFiltersAfterDelete).isEmpty();
    }

    @Test
    @Transactional
    void deleteAllIterable_shouldDeleteSpecifiedFilters() {
        chat = chatRepository.save(chat);
        Filter filter1 = Filter.builder().chat(chat).parameter("param_delete_iter1").value("value_delete_iter1").links(new HashSet<>()).build();
        Filter filter2 = Filter.builder().chat(chat).parameter("param_delete_iter2").value("value_delete_iter2").links(new HashSet<>()).build();
        Filter filter3 = Filter.builder().chat(chat).parameter("param_delete_iter3").value("value_delete_iter3").links(new HashSet<>()).build();
        filter1 = filterRepository.save(filter1);
        filter2 = filterRepository.save(filter2);
        filter3 = filterRepository.save(filter3);

        filterRepository.deleteAll(List.of(filter1, filter3));

        assertThat(filterRepository.findById(filter1.id())).isEmpty();
        assertThat(filterRepository.findById(filter2.id())).isPresent();
        assertThat(filterRepository.findById(filter3.id())).isEmpty();
    }
}
