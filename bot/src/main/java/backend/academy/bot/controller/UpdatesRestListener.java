package backend.academy.bot.controller;

import backend.academy.bot.service.BotService;
import dto.Digest;
import dto.LinkUpdate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings(value = {"CRLF_INJECTION_LOGS"})
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
@Slf4j
@RestController
@RequestMapping("/updates")
@ConditionalOnProperty(value = "app.message.transport", havingValue = "HTTP")
@RequiredArgsConstructor
public class UpdatesRestListener {

    private final BotService service;

    @PostMapping
    public ResponseEntity<Void> receiveUpdate(@RequestBody LinkUpdate update) {
        log.info("Incoming updates: [{}]", update.toString());
        service.processUpdate(update);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/digest")
    public ResponseEntity<Void> receiveDigest(@RequestBody Digest digest) {
        log.info("Incoming digest: [{}]", digest);
        service.processDigests(digest);
        return ResponseEntity.ok().build();
    }
}
