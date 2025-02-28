package backend.academy.scrapper.client;

import org.springframework.web.client.RestClient;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UpdateCheckingClient {
    Optional<LocalDateTime> checkUpdates(String url);
}
