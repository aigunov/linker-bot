package dto;

import java.util.List;
import lombok.Builder;

@Builder
public record Digest(long tgId, List<LinkUpdate> updates) {}
