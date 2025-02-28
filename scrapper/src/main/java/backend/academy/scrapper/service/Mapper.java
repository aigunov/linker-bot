package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class Mapper {
    public Chat chatDtoToEntity(RegisterChatRequest request) {
        return Chat.builder()
            .id(UUID.randomUUID())
            .chatId(request.chatId())
            .username(request.name())
            .creationDate(LocalDateTime.now())
            .build();
    }

    public LinkResponse linkToLinkResponse(Link link) {
        return LinkResponse.builder()
            .id(link.id())
            .url(link.url())
            .filters(link.filters())
            .tags(link.tags())
            .build();

    }

    public Link linkRequestToLink(AddLinkRequest request, Chat chat) {
        return Link.builder()
            .id(UUID.randomUUID())
            .chatId(chat.id())
            .url(request.uri())
            .tags(request.tags())
            .filters(request.filters())
            .lastUpdate(null)
            .build();
    }
}
