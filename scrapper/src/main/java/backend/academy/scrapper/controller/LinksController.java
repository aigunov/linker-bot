package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.ScrapperService;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RemoveLinkRequest;
import io.micrometer.tracing.Link;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/links")
public class LinksController {

    private final ScrapperService scrapperService;

    @GetMapping
    public ResponseEntity<ListLinkResponse> getAllTrackedLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        log.info("Getting all tracked links");
        var response = scrapperService.getAllTrackedLinks(chatId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addTrackedLink(@RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        log.info("Adding tracked link: {}", request);
        LinkResponse response = scrapperService.addTrackedLink(chatId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeTrackedLink(@RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        log.info("Removing tracked link: {}", request);
        LinkResponse response = scrapperService.removeTrackedLink(chatId, request);
        return ResponseEntity.ok(response);
    }

}
