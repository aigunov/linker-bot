package backend.academy.scrapper.controller;

import backend.academy.scrapper.service.LinkService;
import dto.AddLinkRequest;
import dto.GetLinksRequest;
import dto.LinkResponse;
import dto.ListLinkResponse;
import dto.RemoveLinkRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings(value = {"CRLF_INJECTION_LOGS"})
@SuppressFBWarnings(value = {"CRLF_INJECTION_LOGS"})
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/links")
public class LinksController {

    private final LinkService linkService;

    @PostMapping("/getLinks")
    public ResponseEntity<ListLinkResponse> getAllTrackedLinks(
            @RequestHeader("Tg-Chat-Id") Long chatId, @NotNull @RequestBody GetLinksRequest linksRequest) {
        log.info("Getting all tracked links for chat ID: {}", chatId);
        var response = linkService.getAllTrackedLinks(chatId, linksRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addTrackedLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody AddLinkRequest request) {
        log.info("Adding tracked link for chat ID: {}. URI: {}", chatId, request.uri());
        var response = linkService.addTrackedLink(chatId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeTrackedLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        log.info("Removing tracked link for chat ID: {}. URI: {}", chatId, request.uri());
        var response = linkService.removeTrackedLink(chatId, request);
        return ResponseEntity.ok(response);
    }
}
