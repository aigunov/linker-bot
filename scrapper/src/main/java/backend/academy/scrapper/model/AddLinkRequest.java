package backend.academy.scrapper.model;

import java.util.List;
import lombok.Builder;

@Builder
public record AddLinkRequest(String uri, List<String> tags, List<String> filters) { }
