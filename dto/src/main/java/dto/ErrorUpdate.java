package dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ErrorUpdate(UUID id, String url, LocalDateTime timestamp, String error, Set<Long> tgChatIds) {}
