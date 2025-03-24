package backend.academy.bot.clients;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        // arrange
        String json =
                "{\"trace\":\"java.lang.IllegalArgumentException: Invalid argument\\n    at ...\",\"message\":\"Invalid argument\",\"status\":\"400\"}";

        // act
        ApiErrorResponse response = converter.convertJsonToApiErrorResponse(json);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("400");
        assertThat(response.exceptionMessage()).isEqualTo("Invalid argument");
        assertThat(response.exceptionName()).isEqualTo("java.lang.IllegalArgumentException");
        assertThat(response.description()).isEqualTo("Invalid argument");
        assertThat(response.stacktrace())
                .contains("java.lang.IllegalArgumentException: Invalid argument", "    at ...");
    }

    @Test
    void extractExceptionName_shouldExtractNameFromTrace() {
        // arrange
        String trace = "java.lang.IllegalArgumentException: Invalid argument\n    at ...";

        // act
        String exceptionName = converter.extractExceptionName(trace);

        // assert
        assertThat(exceptionName).isEqualTo("java.lang.IllegalArgumentException");
    }

    @Test
    void extractExceptionName_shouldReturnUnknownForEmptyTrace() {
        // arrange
        String emptyTrace = "";

        // act
        String exceptionName = converter.extractExceptionName(emptyTrace);

        // assert
        assertThat(exceptionName).isEqualTo("Unknown Exception");
    }

    @Test
    void convertTraceToList_shouldConvertTraceToList() {
        // arrange
        String trace = "java.lang.IllegalArgumentException: Invalid argument\n    at ...";

        // act
        List<String> traceList = converter.convertTraceToList(trace);

        // assert
        assertThat(traceList).contains("java.lang.IllegalArgumentException: Invalid argument", "    at ...");
    }

    @Test
    void convertTraceToList_shouldReturnEmptyListForEmptyTrace() {
        // arrange
        String emptyTrace = "";

        // act
        List<String> traceList = converter.convertTraceToList(emptyTrace);

        // assert
        assertThat(traceList).isEmpty();
    }

    @Test
    void convertJsonToApiErrorResponse_shouldHandleMissingFields() throws IOException {
        // arrange
        String json = "{\"trace\":\"\",\"message\":\"\",\"status\":\"\"}";

        // act
        ApiErrorResponse response = converter.convertJsonToApiErrorResponse(json);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("");
        assertThat(response.exceptionMessage()).isEqualTo("");
        assertThat(response.exceptionName()).isEqualTo("Unknown Exception");
        assertThat(response.description()).isEqualTo("");
        assertThat(response.stacktrace()).isEmpty();
    }

    @Test
    void extractExceptionName_shouldHandleTraceWithoutColon() {
        // arrange
        String trace = "java.lang.IllegalArgumentException";

        // act
        String exceptionName = converter.extractExceptionName(trace);

        // assert
        assertThat(exceptionName).isEqualTo("java.lang.IllegalArgumentException");
    }

    @Test
    void convertJsonToApiErrorResponse_shouldHandleNullTrace() throws IOException {
        // arrange
        String json = "{\"message\":\"Invalid argument\",\"status\":\"400\"}";

        // act
        ApiErrorResponse response = converter.convertJsonToApiErrorResponse(json);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.code()).isEqualTo("400");
        assertThat(response.exceptionMessage()).isEqualTo("Invalid argument");
        assertThat(response.exceptionName()).isEqualTo("Unknown Exception");
        assertThat(response.description()).isEqualTo("Invalid argument");
        assertThat(response.stacktrace()).isEmpty();
    }
}
