package backend.academy.bot.clients;


import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class JsonToApiErrorResponseTest {

    private JsonToApiErrorResponse converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        converter = new JsonToApiErrorResponse();
        objectMapper = new ObjectMapper();
    }

    @Test
    void convertJsonToApiErrorResponse_shouldConvertValidJson() throws IOException {
        String json = "{\"trace\":\"java.lang.IllegalArgumentException: Invalid argument\\n    at ...\",\"message\":\"Invalid argument\",\"status\":\"400\"}";
        ApiErrorResponse response = converter.convertJsonToApiErrorResponse(json);

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("400");
        assertThat(response.exceptionMessage()).isEqualTo("Invalid argument");
        assertThat(response.exceptionName()).isEqualTo("java.lang.IllegalArgumentException");
        assertThat(response.description()).isEqualTo("Invalid argument");
        assertThat(response.stacktrace()).contains("java.lang.IllegalArgumentException: Invalid argument", "    at ...");
    }

    @Test
    void extractExceptionName_shouldExtractNameFromTrace() {
        String trace = "java.lang.IllegalArgumentException: Invalid argument\n    at ...";
        String exceptionName = converter.extractExceptionName(trace);
        assertThat(exceptionName).isEqualTo("java.lang.IllegalArgumentException");
    }

    @Test
    void extractExceptionName_shouldReturnUnknownForEmptyTrace() {
        String exceptionName = converter.extractExceptionName("");
        assertThat(exceptionName).isEqualTo("Unknown Exception");
    }

    @Test
    void convertTraceToList_shouldConvertTraceToList() {
        String trace = "java.lang.IllegalArgumentException: Invalid argument\n    at ...";
        List<String> traceList = converter.convertTraceToList(trace);
        assertThat(traceList).contains("java.lang.IllegalArgumentException: Invalid argument", "    at ...");
    }

    @Test
    void convertTraceToList_shouldReturnEmptyListForEmptyTrace() {
        List<String> traceList = converter.convertTraceToList("");
        assertThat(traceList).isEmpty();
    }

    @Test
    void convertJsonToApiErrorResponse_shouldHandleMissingFields() throws IOException {
        String json = "{\"trace\":\"\",\"message\":\"\",\"status\":\"\"}";
        ApiErrorResponse response = converter.convertJsonToApiErrorResponse(json);

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("");
        assertThat(response.exceptionMessage()).isEqualTo("");
        assertThat(response.exceptionName()).isEqualTo("Unknown Exception");
        assertThat(response.description()).isEqualTo("");
        assertThat(response.stacktrace()).isEmpty();
    }

    @Test
    void extractExceptionName_shouldHandleTraceWithoutColon() {
        String trace = "java.lang.IllegalArgumentException";
        String exceptionName = converter.extractExceptionName(trace);
        assertThat(exceptionName).isEqualTo("java.lang.IllegalArgumentException");
    }

    @Test
    void convertJsonToApiErrorResponse_shouldHandleNullTrace() throws IOException {
        String json = "{\"message\":\"Invalid argument\",\"status\":\"400\"}";
        ApiErrorResponse response = converter.convertJsonToApiErrorResponse(json);

        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("400");
        assertThat(response.exceptionMessage()).isEqualTo("Invalid argument");
        assertThat(response.exceptionName()).isEqualTo("Unknown Exception");
        assertThat(response.description()).isEqualTo("Invalid argument");
        assertThat(response.stacktrace()).isEmpty();
    }
}
