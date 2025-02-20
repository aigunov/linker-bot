package backend.academy.scrapper.controller;

import dto.ApiErrorResponse;
import dto.RegisterChatRequest;
import backend.academy.scrapper.service.ScrapperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/tg-chat")
public class TgChatController {

    private final ScrapperService scrapperService;

    @PostMapping("/{id}")
    public ResponseEntity<?> registerChat(@PathVariable Long id, @Valid @RequestBody RegisterChatRequest request) {
        try {
            scrapperService.registerChat(id, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка регистрации чата: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(createErrorResponse(e, "400"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChat(@PathVariable Long id) {
        try {
            scrapperService.deleteChat(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Ошибка удаления чата: {}", e.getMessage(), e);
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
