package backend.academy.scrapper.model;

import lombok.Builder;

@Builder
public record RegisterChatRequest(Long id, String name) {
}
