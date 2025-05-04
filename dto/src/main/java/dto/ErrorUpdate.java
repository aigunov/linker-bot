package dto;

import lombok.Builder;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record ErrorUpdate(UUID id, String url, LocalDateTime timestamp, String error, Set<Long> tgChatIds) {
}
