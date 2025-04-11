package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapperTest {

    private Mapper mapper;
    private RegisterChatRequest registerChatRequest;
    private AddLinkRequest addLinkRequest;
    private Chat chat;
    private Link link;
    private Tag tag1;
    private Tag tag2;
    private Filter filter1;
    private Filter filter2;

    @BeforeEach
    void setUp() {
        mapper = new Mapper();
        registerChatRequest = new RegisterChatRequest(123L, "testUser");
        addLinkRequest = new AddLinkRequest("https://example.com", List.of("tag1", "tag2"), List.of("param1:value1", "param2:value2"));

        chat = Chat.builder()
            .id(UUID.randomUUID())
            .tgId(123L)
            .nickname("testChat")
            .tags(new HashSet<>())
            .filters(new HashSet<>())
            .links(new HashSet<>())
            .build();

        tag1 = Tag.builder().id(UUID.randomUUID()).tag("tag1").chat(chat).links(new HashSet<>()).build();
        tag2 = Tag.builder().id(UUID.randomUUID()).tag("tag2").chat(chat).links(new HashSet<>()).build();
        filter1 = Filter.builder().id(UUID.randomUUID()).parameter("param1").value("value1").chat(chat).links(new HashSet<>()).build();
        filter2 = Filter.builder().id(UUID.randomUUID()).parameter("param2").value("value2").chat(chat).links(new HashSet<>()).build();

        link = Link.builder()
            .id(UUID.randomUUID())
            .url("https://example.com")
            .lastUpdate(LocalDateTime.now())
            .tags(Set.of(tag1, tag2))
            .filters(Set.of(filter1, filter2))
            .chats(Set.of(chat))
            .build();
    }

    @Test
    void chatDtoToEntity_shouldMapRegisterChatRequestToChat() {
        // act
        Chat mappedChat = Mapper.chatDtoToEntity(registerChatRequest);

        // assert
        assertThat(mappedChat)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("tgId", 123L)
            .hasFieldOrPropertyWithValue("nickname", "testUser")
            .hasFieldOrProperty("tags")
            .hasFieldOrProperty("filters")
            .hasFieldOrProperty("links");
        assertThat(mappedChat.tags()).isEmpty();
        assertThat(mappedChat.filters()).isEmpty();
        assertThat(mappedChat.links()).isEmpty();
    }

    @Test
    void linkToLinkResponse_shouldMapLinkToLinkResponse() {
        // act
        LinkResponse mappedLinkResponse = Mapper.linkToLinkResponse(link);

        // assert
        assertThat(mappedLinkResponse)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", link.id())
            .hasFieldOrPropertyWithValue("url", "https://example.com")
            .hasFieldOrPropertyWithValue("tags", List.of("tag1", "tag2"))
            .hasFieldOrPropertyWithValue("filters", List.of("param1:value1", "param2:value2"));
    }

    @Test
    void linkRequestToLink_shouldMapAddLinkRequestAndChatToLink() {
        // arrange
        HashSet<Tag> tags = new HashSet<>(List.of(Tag.builder().tag("tag1").chat(chat).links(new HashSet<>()).build(),
            Tag.builder().tag("tag2").chat(chat).links(new HashSet<>()).build()));
        HashSet<Filter> filters = new HashSet<>(List.of(Filter.builder().parameter("param1").value("value1").chat(chat).links(new HashSet<>()).build(),
            Filter.builder().parameter("param2").value("value2").chat(chat).links(new HashSet<>()).build()));

        // act
        Link mappedLink = Mapper.linkRequestToLink(addLinkRequest.uri(), chat, tags, filters);

        // assert
        assertThat(mappedLink)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("url", "https://example.com")
            .hasFieldOrProperty("lastUpdate")
            .hasFieldOrProperty("tags")
            .hasFieldOrProperty("filters")
            .hasFieldOrProperty("chats");
        assertThat(mappedLink.chats()).containsExactly(chat);
        assertThat(mappedLink.tags()).hasSize(2)
            .extracting(Tag::tag)
            .containsExactlyInAnyOrder("tag1", "tag2");
        assertThat(mappedLink.filters()).hasSize(2)
            .extracting(f -> f.parameter() + ":" + f.value())
            .containsExactlyInAnyOrder("param1:value1", "param2:value2");
        assertThat(mappedLink.lastUpdate()).isNotNull();
    }

    @Test
    void tagDtoToTag_shouldMapStringToTagAndChat() {
        // arrange
        String tagName = "newTag";

        // act
        Tag mappedTag = Mapper.tagDtoToTag(tagName, chat);

        // assert
        assertThat(mappedTag)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("tag", "newTag")
            .hasFieldOrPropertyWithValue("chat", chat)
            .hasFieldOrProperty("links");
        assertThat(mappedTag.links()).isEmpty();
    }

    @Test
    void filterDtoToFilter_shouldMapParameterValueAndChatToFilter() {
        // arrange
        String param = "priority";
        String val = "high";

        // act
        Filter mappedFilter = Mapper.filterDtoToFilter(param, val, chat);

        // assert
        assertThat(mappedFilter)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("parameter", "priority")
            .hasFieldOrPropertyWithValue("value", "high")
            .hasFieldOrPropertyWithValue("chat", chat)
            .hasFieldOrProperty("links");
        assertThat(mappedFilter.links()).isEmpty();
    }
}
