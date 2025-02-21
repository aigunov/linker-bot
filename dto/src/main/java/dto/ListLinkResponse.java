package dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
public record ListLinkResponse(List<LinkResponse> linkResponses, int size) {
}

