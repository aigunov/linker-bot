package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import dto.AddLinkRequest;
import dto.LinkResponse;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class Mapper {
    public Chat chatDtoToEntity(Optional<Chat> chatOpt) {
        return null;
    }

    public LinkResponse linkToLinkResponse(Link link){
        return null;
    }

    public Link linkRequestToLink(AddLinkRequest request) {
        return null;
    }
}
