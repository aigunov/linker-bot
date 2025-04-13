package dto;

import java.util.List;
import lombok.Builder;

@Builder
public record GetTagsResponse(List<String> tags) {}
