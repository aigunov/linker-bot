package dto;

import java.util.List;
import java.util.UUID;

public record LinkUpdate(
    UUID id,
    String url,
    String description,
    List<Long> tgChatIds
) {}
