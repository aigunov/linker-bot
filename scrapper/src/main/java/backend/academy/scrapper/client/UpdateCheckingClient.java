package backend.academy.scrapper.client;

import backend.academy.scrapper.data.dto.UpdateInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;

public interface UpdateCheckingClient {
    Optional<UpdateInfo> checkUpdates(String url) throws JsonProcessingException;
}
