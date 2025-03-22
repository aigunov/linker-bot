package dto;

import java.util.List;
import lombok.Builder;

@Builder
public record ListLinkResponse(List<LinkResponse> linkResponses, int size) {}
