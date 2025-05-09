package dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record DigestRecord(Long chatId, String url, String message, UUID linkId) {
}
