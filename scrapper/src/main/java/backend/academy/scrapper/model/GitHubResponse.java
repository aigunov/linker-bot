package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitHubResponse {
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;

    public LocalDateTime getUpdatedAt() {
        return updatedAt != null ? updatedAt.atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() : null;
    }
}
