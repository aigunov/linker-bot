package dto;

import java.util.List;
import lombok.Builder;

/**
 * Represents an API error response.
 *
 * @param description A description of the error.
 * @param code The error code.
 * @param exceptionName The name of the exception.
 * @param exceptionMessage The message of the exception.
 * @param stacktrace The stack trace of the exception.
 */
@Builder
public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {}
