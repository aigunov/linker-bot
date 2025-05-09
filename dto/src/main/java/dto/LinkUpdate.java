package dto;

import java.util.Set;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record LinkUpdate(UUID id, String url, String message, Set<Long> tgChatIds) {}
