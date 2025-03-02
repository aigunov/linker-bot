package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ScrapperService;
import dto.RegisterChatRequest;
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

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tg-chat")
public class TgChatController {

    private final ScrapperService scrapperService;

    @PostMapping("/{id}")
    public ResponseEntity<String> registerChat(@PathVariable Long id, @Valid @RequestBody RegisterChatRequest request) {
        log.info("Registering chat with chatId {}", id);
        var responseString = scrapperService.registerChat(id, request);
        return ResponseEntity.ok().body(responseString);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id) {
        log.info("Deleting chat with chatId {}", id);
        var responseString = scrapperService.deleteChat(id);
        return ResponseEntity.ok().body(responseString);
    }
}
