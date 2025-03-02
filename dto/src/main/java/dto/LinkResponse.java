package dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record LinkResponse(UUID id, String url, List<String> tags, List<String> filters) {}
