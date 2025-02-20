package dto;

import lombok.Builder;

@Builder
public record RegisterChatRequest(Long id, String name) {
}
