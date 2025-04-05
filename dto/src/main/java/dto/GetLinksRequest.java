package dto;

import lombok.Builder;
import java.util.List;

//todo: добавятся еще и список фильтров
@Builder
public record GetLinksRequest(
    List<String> tags
) {
}
