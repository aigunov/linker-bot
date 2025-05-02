package dto;

import java.util.List;
import lombok.Builder;

// todo: добавятся еще и список фильтров
@Builder
public record GetLinksRequest(List<String> tags) {}
