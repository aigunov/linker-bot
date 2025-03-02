package backend.academy.scrapper.client;

import backend.academy.scrapper.service.LinkToApiRequestConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public abstract class AbstractUpdateCheckingClient implements UpdateCheckingClient {
    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final RestClient restClient;
    protected final LinkToApiRequestConverter converterApi;
}
