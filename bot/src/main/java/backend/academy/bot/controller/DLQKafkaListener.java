package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import dto.ErrorUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DLQKafkaListener {

    private final BotService service;

    @KafkaListener(topics = "${app.message.kafka.topic.dead-letter}", groupId = "bot-consumer",
        containerFactory = "errorMessageContainerFactory")
    public void handleError(ErrorUpdate errorMessage) {
        log.warn("Kafka DLQ: received error message: {}", errorMessage);
        //todo: добавить обработку исключений
    }
}
