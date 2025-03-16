package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.UpdateInfo;
import java.time.LocalDateTime;
import java.util.Optional;

public interface UpdateCheckingClient {
    Optional<UpdateInfo> checkUpdates(String url);
}
