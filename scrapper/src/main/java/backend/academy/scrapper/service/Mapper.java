package backend.academy.scrapper.service;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class Mapper {
    //TODO: переделать
    public Chat chatDtoToEntity(RegisterChatRequest request) {
        return null;
    }

    //TODO: переделать
    public LinkResponse linkToLinkResponse(Link link) {
        return null;
    }

    //TODO: переделать
    public Link linkRequestToLink(AddLinkRequest request, Chat chat) {
        return null;
    }
}
