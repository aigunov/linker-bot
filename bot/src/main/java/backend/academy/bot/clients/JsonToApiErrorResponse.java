package backend.academy.bot.clients;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JsonToApiErrorResponse {

    public ApiErrorResponse convertJsonToApiErrorResponse(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonString);

        String trace = rootNode.path("trace").asText();
        String message = rootNode.path("message").asText();
        String status = rootNode.path("status").asText();

        var error = ApiErrorResponse.builder()
                .code(status.isEmpty() ? rootNode.path("code").asText() : status)
                .exceptionMessage(message)
                .exceptionName(extractExceptionName(trace))
                .description(message.isEmpty() ? rootNode.path("description").asText() : message)
                .stacktrace(convertTraceToList(trace))
                .build();
        log.info("Converted Json to error: {}", error);
        return error;
    }

    public String extractExceptionName(String trace) {
        if (trace == null || trace.isEmpty()) {
            return "Unknown Exception";
        }
        String[] lines = trace.split("\\r?\\n");
        if (lines.length > 0) {
            String firstLine = lines[0];
            int colonIndex = firstLine.indexOf(':');
            if (colonIndex > 0) {
                return firstLine.substring(0, colonIndex);
            } else {
                return firstLine.split(":")[0];
            }
        }
        return "Unknown Exception";
    }

    public List<String> convertTraceToList(String trace) {
        if (trace == null || trace.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(trace.split("\\r?\\n")).collect(Collectors.toList());
    }
}
