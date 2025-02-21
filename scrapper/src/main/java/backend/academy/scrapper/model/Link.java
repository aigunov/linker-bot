package backend.academy.scrapper.model;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Link {
    private UUID id;
    private UUID userId;
    private String url;
    private List<String> tags;
    private List<String> filters;
}
