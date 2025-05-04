package dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ErrorUpdate(String url, LocalDateTime timestamp, String error) {
}
