package backend.academy.scrapper.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.message.kafka.topic.notification}")
    private String notificationTopic;

    @Value("${app.message.kafka.topic.dead-letter}")
    private String deadLetterTopic;

    @Value("${app.message.kafka.topic.digest}")
    private String digestTopic;

    @Bean
    public NewTopic notificationTopic(){
        return TopicBuilder.name(notificationTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic digestTopic(){
        return TopicBuilder.name(digestTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
