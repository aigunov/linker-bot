package backend.academy.scrapper.service;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import org.springframework.stereotype.Service;
import java.util.HashSet;

@Service
public class Mapper {
    //TODO: переделать
    public static Chat chatDtoToEntity(RegisterChatRequest request) {
        return null;
    }

    //TODO: переделать
    public static LinkResponse linkToLinkResponse(Link link) {
        return null;
    }

    //TODO: переделать
    public static Link linkRequestToLink(final AddLinkRequest request, final Long chatId) {
        return null;
    }

    public static Tag tagDtoToTag(final String tag, final Chat chat) {return null;}

    public static Filter filterDtoToFilter(String filterParam, String filterName, Chat chat) {return null;
    }

    public static Link linkRequestToLink(String uri, Long chatId, HashSet<Tag> tags, HashSet<Filter> filters) {
        return null;
    }
}
