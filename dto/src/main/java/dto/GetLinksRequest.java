package dto;

import java.util.List;
import lombok.Builder;

@Builder
public record GetLinksRequest(List<String> tags, List<String> filters) {}
