package dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record DigestRecord(Long chatId, String url, String message, UUID linkId) {}
