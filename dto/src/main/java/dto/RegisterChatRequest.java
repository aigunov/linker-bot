package dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public record RegisterChatRequest(Long id, String name) {
}
