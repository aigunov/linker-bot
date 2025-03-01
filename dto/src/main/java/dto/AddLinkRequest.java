package dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Builder
public record AddLinkRequest(
    String uri,
    List<String> tags,
    List<String> filters
) {

    public AddLinkRequest(String uri, List<String> tags, List<String> filters) {
        this.uri = uri;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.filters = filters != null ? filters : new ArrayList<>();
    }
}
