package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.TgChatService;
import dto.NotificationTimeRequest;
import dto.RegisterChatRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings(value = {"CRLF_INJECTION_LOGS"})
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tg-chat")
public class TgChatController {

    private final TgChatService tgChatService;

    @PostMapping("/{chatId}")
    public ResponseEntity<String> registerChat(
            @PathVariable Long chatId, @Valid @RequestBody RegisterChatRequest request) {
        log.info("Registering chat with ID: {}. Name: {}", chatId, request.name());
        var responseString = tgChatService.registerChat(chatId, request);
        return ResponseEntity.ok().body(responseString);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id) {
        log.info("Deleting chat with ID: {}", id);
        var responseString = tgChatService.deleteChat(id);
        return ResponseEntity.ok().body(responseString);
    }

    @PostMapping("/time/{chatId}")
    public ResponseEntity<String> changeDigestTime(
            @PathVariable Long chatId, @Valid @RequestBody NotificationTimeRequest request) {
        log.info("Set new digest time {} for chat with tgId: {}", request.time(), chatId);
        var responseString = tgChatService.setDigestTime(chatId, request);
        return ResponseEntity.ok().body(responseString);
    }
}
