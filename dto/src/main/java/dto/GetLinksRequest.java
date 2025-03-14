package dto;

import lombok.Builder;
import java.util.List;

@Builder
public record GetLinksRequest(
    List<String> tags
) {
}
