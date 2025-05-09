package dto;

import lombok.Builder;
import java.util.List;

@Builder
public record Digest(long tgId, List<LinkUpdate> updates) {
}
