package backend.academy.scrapper.model;

import java.util.List;
import lombok.Builder;

@Builder
public class ApiErrorResponse {
    private String description;
    private String code;
    private String exceptionName;
    private String exceptionMessage;
    private List<String> stacktrace;
}
