package backend.academy.scrapper.cache;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import backend.academy.scrapper.data.dto.UpdateInfo;
import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.service.RedisService;
import dto.DigestRecord;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

@SpringBootTest
@Testcontainers
@TestPropertySource(
        properties = {
            "app.db.access-type=orm",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.hibernate.ddl-auto=create",
            "app.scrapper.page-size=10",
            "app.scrapper.threads-count=1",
            "app.scrapper.scheduled-time=100000",
            "app.digest.threads-count=4",
            "app.digest.scheduler-time=60000"
        })
public class RedisCacheIntegrationTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "12345");

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.username", () -> "");
        registry.add("spring.data.redis.password", () -> "12345");

        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("app.message.transport", () -> "kafka");
    }

    @Autowired
    private RedisService redisService;

    private final LocalTime testTime = LocalTime.of(10, 0);
    private final String expectedKey = "digest:10:00";

    @Autowired
    private RedisTemplate<String, DigestRecord> redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.delete(expectedKey);
    }

    @Test
    void shouldStoreDigestRecord() {
        DigestRecord record = DigestRecord.builder()
                .chatId(1001L)
                .url("https://github.com/test/repo")
                .message("Test issue")
                .linkId(UUID.randomUUID())
                .build();

        redisService.storeUpdate(
                Set.of(mockChat(1001L)), mockLink(record.url(), record.linkId()), mockUpdate(record.message()));

        List<DigestRecord> results = redisTemplate.opsForList().range(expectedKey, 0, -1);
        var actualDigest = results.getFirst();

        assertThat(results).hasSize(1);
        assertThat(actualDigest.chatId()).isEqualTo(record.chatId());
        assertThat(actualDigest.url()).isEqualTo(record.url());
        assertThat(actualDigest.linkId()).isEqualTo(record.linkId());
    }

    @Test
    void shouldConsumerRecordsGroupeByChatId() {
        DigestRecord rec1 = DigestRecord.builder()
                .chatId(1L)
                .url("url1")
                .message("msg1")
                .linkId(UUID.randomUUID())
                .build();
        DigestRecord rec2 = DigestRecord.builder()
                .chatId(1L)
                .url("url2")
                .message("msg2")
                .linkId(UUID.randomUUID())
                .build();
        DigestRecord rec3 = DigestRecord.builder()
                .chatId(2L)
                .url("url3")
                .message("msg3")
                .linkId(UUID.randomUUID())
                .build();

        redisTemplate.opsForList().rightPushAll(expectedKey, rec1, rec2, rec3);

        Map<Long, List<DigestRecord>> result = redisService.consumeForTime(testTime);

        assertThat(result).containsKeys(1L, 2L);
        assertThat(result.get(1L)).hasSize(2);
        assertThat(result.get(2L)).hasSize(1);
    }

    @Test
    void shouldClearDigestKey() {
        DigestRecord record = DigestRecord.builder()
                .chatId(200L)
                .url("url")
                .message("msg")
                .linkId(UUID.randomUUID())
                .build();

        redisTemplate.opsForList().rightPush(expectedKey, record);

        redisService.clearDigestTimeKey(testTime);

        List<DigestRecord> results = redisTemplate.opsForList().range(expectedKey, 0, -1);
        assertThat(results).isEmpty();
    }

    private Chat mockChat(Long tgId) {
        return Chat.builder().tgId(tgId).digestTime(testTime).build();
    }

    private Link mockLink(String url, UUID id) {
        return Link.builder().id(id).url(url).build();
    }

    private UpdateInfo mockUpdate(String msg) {
        return UpdateInfo.builder()
                .title("Mock title")
                .username("mockUser")
                .type("issue")
                .preview("mock preview")
                .date(LocalDateTime.now())
                .build();
    }
}
