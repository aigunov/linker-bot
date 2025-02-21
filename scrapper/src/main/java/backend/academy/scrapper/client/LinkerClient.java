package backend.academy.scrapper.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

public interface LinkerClient {
    WebClient webClient = null;

    <T, E>ResponseEntity<Object> makeAndSendRequest(String uri,
                                                    HttpMethod httpMethod,
                                                    Map<String, String> headers,
                                                    T body,
                                                    Class<E> responseClass,
                                                    Object... uriParameters);
}
