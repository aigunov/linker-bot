package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import dto.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/updates")
@RequiredArgsConstructor
public class UpdatesController {

    private final BotService service;

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody LinkUpdate update) {
        log.info("Incoming updates: {}", update);
        service.processUpdate(update);
        return ResponseEntity.ok().build();
    }
}
