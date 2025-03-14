package dto;

import lombok.Builder;
import java.util.List;
@Builder
public record GetTagsResponse(
    List<String> tags
) {
}
