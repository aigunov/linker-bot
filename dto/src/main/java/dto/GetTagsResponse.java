package dto;

import java.util.List;

public record GetTagsResponse(
    List<String> tags
) {
}
