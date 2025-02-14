package backend.academy.bot.model;

import lombok.Builder;
import java.util.List;

@Builder
public class ApiErrorResponse {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
    private List<String> stacktrace;
}
