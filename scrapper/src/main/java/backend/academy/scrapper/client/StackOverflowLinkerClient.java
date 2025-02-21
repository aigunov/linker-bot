package backend.academy.scrapper.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.Map;

public class StackOverflowLinkerClient implements LinkerClient {
    @Override
    public <T, E> ResponseEntity<Object> makeAndSendRequest(String uri, HttpMethod httpMethod, Map<String, String> headers, T body, Class<E> responseClass, Object... uriParameters) {
        return null;
    }
}
