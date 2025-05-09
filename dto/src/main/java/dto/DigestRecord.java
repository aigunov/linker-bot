package dto;

import lombok.Builder;

@Builder
public record DigestRecord(Long chatId, String url, String message) {
}
