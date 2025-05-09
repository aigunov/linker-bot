package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import dto.Digest;
import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@ConditionalOnProperty(value = "app.message.transport", havingValue = "Kafka")
@RequiredArgsConstructor
public class UpdatesKafkaListener {

    private final BotService service;

    @KafkaListener(topics = "${app.message.kafka.topic.notification}", groupId = "bot-consumer",
        containerFactory = "kafkaListenerContainerFactory")
    public void listenNotifications(LinkUpdate update) {
        log.info("Incoming link update: [{}]", update);
        service.processUpdate(update);
    }


    @KafkaListener(
        topics = "${app.message.kafka.topic.digest}",
        groupId = "digest-group",
        containerFactory = "digestKafkaListenerContainerFactory")
    public void listenDigests(Digest digest) {
        log.info("Incoming digests: [{}]", digest);
        service.processDigests(digest);
    }


}
