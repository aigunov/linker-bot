package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Data
public class GitHubResponse {
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    public LocalDateTime getUpdatedAt() {
        return updatedAt != null ? updatedAt.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }
}
