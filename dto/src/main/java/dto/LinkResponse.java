package dto;

import lombok.Builder;
import java.util.List;
import java.util.UUID;

@Builder
public record LinkResponse(
    UUID id,
    String url,
    List<String> tags,
    List<String> filters) {
}
