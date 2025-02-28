package dto;

import lombok.Builder;

@Builder
public record RegisterChatRequest(Long chatId, String name) {
}
