package dto;

import lombok.Builder;

@Builder
public record RemoveLinkRequest (String uri){
}
