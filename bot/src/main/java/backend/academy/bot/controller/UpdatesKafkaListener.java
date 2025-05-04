package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import dto.ErrorUpdate;
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
    public void listen(LinkUpdate update) {
        log.info("Incoming link update: [{}]", update);
        service.processUpdate(update);
    }


    @KafkaListener(topics = "${app.message.kafka.topic.dead-letter}", groupId = "bot-consumer",
        containerFactory = "errorMessageContainerFactory")
    public void handleError(ErrorUpdate errorMessage) {
        log.warn("Kafka DLQ: received error message: {}", errorMessage);
        //todo: добавить обработку исключений
    }
}
