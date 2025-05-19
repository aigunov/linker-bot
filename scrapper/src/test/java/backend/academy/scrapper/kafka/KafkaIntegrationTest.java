package backend.academy.scrapper.kafka;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import dto.ErrorUpdate;
import dto.LinkUpdate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
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
public class KafkaIntegrationTest {

    @Container
    static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @Container
    static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:17.4")
            .withDatabaseName("scrapper_db")
            .withUsername("aigunov")
            .withPassword("12345");

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.message.kafka.topic.dead-letter}")
    private String deadLetterTopic;

    @DynamicPropertySource
    static void overrideKafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("app.message.transport", () -> "kafka");
    }

    @Test
    void shouldSendLinkUpdateSuccessfullyToNotificationTopic() {
        LinkUpdate update = LinkUpdate.builder()
                .id(UUID.randomUUID())
                .url("https://github.com/test/repo")
                .message("Issue opened")
                .tgChatIds(Set.of(123L, 456L))
                .build();

        kafkaTemplate.send(notificationTopic, update);

        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-group-1", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, LinkUpdate.class.getName());
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        try (Consumer<String, LinkUpdate> consumer =
                new DefaultKafkaConsumerFactory<String, LinkUpdate>(consumerProps).createConsumer()) {
            consumer.subscribe(Collections.singleton(notificationTopic));

            ConsumerRecord<String, LinkUpdate> record =
                    KafkaTestUtils.getSingleRecord(consumer, notificationTopic, Duration.ofSeconds(10));

            assertThat(record.value()).isNotNull();
            assertThat(record.value().url()).isEqualTo("https://github.com/test/repo");
            assertThat(record.value().message()).contains("Issue");
            //            assertThat(record.value().tgChatIds()).contains(123L, 456L);
        }
    }

    @Test
    void shouldSendErrorUpdateSuccessfullyToDLQ() {
        ErrorUpdate errorUpdate = ErrorUpdate.builder()
                .id(UUID.randomUUID())
                .url("https://stackoverflow.com/q/123")
                .timestamp(LocalDateTime.now())
                .error("Failed to parse response")
                .tgChatIds(Set.of(321L))
                .build();

        kafkaTemplate.send(deadLetterTopic, errorUpdate);

        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps(kafka.getBootstrapServers(), "test-group-2", "true");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ErrorUpdate.class.getName());
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        try (Consumer<String, ErrorUpdate> consumer =
                new DefaultKafkaConsumerFactory<String, ErrorUpdate>(consumerProps).createConsumer()) {
            consumer.subscribe(Collections.singleton(deadLetterTopic));

            ConsumerRecord<String, ErrorUpdate> record =
                    KafkaTestUtils.getSingleRecord(consumer, deadLetterTopic, Duration.ofSeconds(10));

            assertThat(record.value()).isNotNull();
            assertThat(record.value().error()).contains("Failed to parse");
            //            assertThat(record.value().tgChatIds()).contains(321L);
        }
    }
}
