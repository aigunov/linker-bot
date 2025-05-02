package backend.academy.scrapper.service;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class Mapper {

    public static Chat chatDtoToEntity(final RegisterChatRequest request) {
        return Chat.builder()
                .nickname(request.name())
                .tgId(request.chatId())
                .tags(new HashSet<>())
                .filters(new HashSet<>())
                .links(new HashSet<>())
                .build();
    }

    public static LinkResponse linkToLinkResponse(Link link) {
        return LinkResponse.builder()
                .id(link.id())
                .url(link.url())
                .tags(link.tags().stream().map(Tag::tag).toList())
                .filters(link.filters().stream()
                        .map(filter -> filter.parameter() + ":" + filter.value())
                        .toList())
                .build();
    }

    public static Link linkRequestToLink(
            final String uri, final Chat chat, final Set<Tag> tags, final Set<Filter> filters) {
        return Link.builder()
                .url(uri)
                .lastUpdate(LocalDateTime.now())
                .tags(tags)
                .filters(filters)
                .chats(Set.of(chat))
                .build();
    }

    public static Tag tagDtoToTag(final String tag, final Chat chat) {
        return Tag.builder().tag(tag).chat(chat).links(new HashSet<>()).build();
    }

    public static Filter filterDtoToFilter(final String parameter, final String value, final Chat chat) {
        return Filter.builder()
                .value(value)
                .parameter(parameter)
                .chat(chat)
                .links(new HashSet<>())
                .build();
    }
}
