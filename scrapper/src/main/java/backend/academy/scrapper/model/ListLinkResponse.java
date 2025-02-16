package backend.academy.scrapper.model;

import java.util.List;

public record ListLinkResponse(List<LinkResponse> linkResponses, int size) {
}
