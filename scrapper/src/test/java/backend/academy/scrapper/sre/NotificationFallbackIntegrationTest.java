package backend.academy.scrapper.sre;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.ScrapperApplication;
import backend.academy.scrapper.client.RestNotificationClient;
import dto.LinkUpdate;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

@Slf4j
@Testcontainers
@SpringBootTest(classes = ScrapperApplication.class)
@TestPropertySource(
        properties = {
            "app.db.access-type=orm",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.hibernate.ddl-auto=create",
            "app.scrapper.page-size=10",
            "app.scrapper.threads-count=1",
            "app.scrapper.scheduled-time=100000",
            "app.digest.threads-count=4",
            "app.digest.scheduler-time=60000",
            "app.message.transport=HTTP",
            "app.message.kafka.topic.notification=test-notification",
            "app.message.kafka.topic.digest=test-digest",
            "app.message.kafka.topic.dead-letter=test-dead-letter",
            "app.bot.url=http://localhost:9999"
        })
public class NotificationFallbackIntegrationTest {

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Autowired
    private RestNotificationClient restNotificationClient;

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.data.redis.username", () -> "aigunov");
        registry.add("spring.data.redis.password", () -> "12345");

        registry.add("client.resilience.bot-client.timeout", () -> "1s");
        registry.add("client.resilience.bot-client.wait-duration", () -> "1s");
        registry.add("client.resilience.bot-client.max-attempts", () -> "2");
    }

    @Test
    void shouldFallbackToKafka_whenHttpFails() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-fallback-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, LinkUpdate.class.getName());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        try (Consumer<String, LinkUpdate> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(notificationTopic));

            LinkUpdate update = LinkUpdate.builder()
                    .id(UUID.randomUUID())
                    .url("https://example.com/fallback")
                    .message("Test fallback message")
                    .tgChatIds(Set.of(1L, 2L))
                    .build();

            restNotificationClient.sendLinkUpdate(update);

            ConsumerRecords<String, LinkUpdate> records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.count()).isGreaterThan(0);

            LinkUpdate received = records.iterator().next().value();
            assertThat(received.url()).isEqualTo(update.url());
            assertThat(received.message()).isEqualTo(update.message());
            assertThat(received.tgChatIds()).containsExactlyInAnyOrderElementsOf(update.tgChatIds());
        }
    }
}
