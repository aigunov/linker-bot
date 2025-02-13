package backend.academy.bot.model;

import lombok.Builder;

@Builder
public record RegisterChatRequest(Long id, String name) {
}
