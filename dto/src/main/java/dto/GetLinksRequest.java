package dto;

import java.util.List;

public record GetLinksRequest(
    List<String> tags
) {
}
