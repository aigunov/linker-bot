package backend.academy.bot.controller;

import backend.academy.bot.exception.FailedIncomingUpdatesHandleException;
import dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(FailedIncomingUpdatesHandleException.class)
    public ResponseEntity<ApiErrorResponse> handleFailedIncomingUpdates(FailedIncomingUpdatesHandleException e) {
        ApiErrorResponse errorResponse = createErrorResponse(e, "UPDATE_SEND_FAILED");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
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
