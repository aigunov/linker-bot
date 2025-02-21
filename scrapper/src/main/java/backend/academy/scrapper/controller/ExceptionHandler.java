package backend.academy.scrapper.controller;

import dto.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler {


    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception e) {
        log.error("Произошла ошибка: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(createErrorResponse(e, "400"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoSuchElementException e) {
        log.error("Объект не найден: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e, "404"));
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
