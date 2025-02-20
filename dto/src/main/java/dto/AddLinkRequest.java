package dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class AddLinkRequest {
    private String uri;
    private List<String> tags;
    private List<String> filters;
}
