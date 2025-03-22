package backend.academy.scrapper.controller;

import dto.ApiErrorResponse;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.AopInvocationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice("backend.academy.scrapper.controller")
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(AopInvocationException.class)
    public ResponseEntity<ApiErrorResponse> handleAopException(AopInvocationException e) {
        log.error("Ошибка AOP: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e, "500"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception e) {
        var errorResponse = createErrorResponse(e, "400");
        log.error("Произошла ошибка: {}", errorResponse);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NoSuchElementException e) {
        log.error("Объект не найден: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e, "404"));
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("IllegalArgumentException: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(exception, "500"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> handleAll(Throwable e) {
        log.error("Неизвестная ошибка: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse(e, "500"));
    }

    private ApiErrorResponse createErrorResponse(Throwable e, String code) {
        return ApiErrorResponse.builder()
                .description(e.getMessage())
                .code(code)
                .exceptionName(e.getClass().getSimpleName())
                .exceptionMessage(e.getMessage())
                .build();
    }
}
