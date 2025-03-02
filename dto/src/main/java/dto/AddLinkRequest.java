package dto;

import java.util.List;
import lombok.Builder;

/**
 * Record representing a request to add a link with optional tags and filters.
 *
 * @param uri The URI of the link.
 * @param tags The list of tags (can be null).
 * @param filters The list of filters (can be null).
 */
@Builder
public record AddLinkRequest(String uri, List<String> tags, List<String> filters) {}
