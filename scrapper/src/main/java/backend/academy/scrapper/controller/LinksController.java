package backend.academy.scrapper.controller;

import backend.academy.scrapper.model.AddLinkRequest;
import backend.academy.scrapper.model.ApiErrorResponse;
import backend.academy.scrapper.model.LinkResponse;
import backend.academy.scrapper.model.ListLinkResponse;
import backend.academy.scrapper.model.RemoveLinkRequest;
import backend.academy.scrapper.service.ScrapperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/links") // Общий путь для контроллера LinksController
public class LinksController {

    private final ScrapperService scrapperService;

    @GetMapping
    public ResponseEntity<?> getAllTrackedLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        try {
            ListLinkResponse response = scrapperService.getAllTrackedLinks(chatId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка получения ссылок: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(createErrorResponse(e, "400"));
        }
    }

    @PostMapping
    public ResponseEntity<?> addTrackedLink(@RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody AddLinkRequest request) {
        try {
            LinkResponse response = scrapperService.addTrackedLink(chatId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка добавления ссылки: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(createErrorResponse(e, "400"));
        }
    }

    @DeleteMapping
    public ResponseEntity<?> removeTrackedLink(@RequestHeader("Tg-Chat-Id") Long chatId, @Valid @RequestBody RemoveLinkRequest request) {
        try {
            LinkResponse response = scrapperService.removeTrackedLink(chatId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Ошибка удаления ссылки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e, "404"));
        }
    }

    private ApiErrorResponse createErrorResponse(Exception e, String code) {
        return ApiErrorResponse.builder()
            .description(e.getMessage())
            .code(code)
            .exceptionName(e.getClass().getSimpleName())
            .exceptionMessage(e.getMessage())
            .build();
    }
}
