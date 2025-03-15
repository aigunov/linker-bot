package backend.academy.scrapper.service;

import backend.academy.scrapper.model.Chat;
import backend.academy.scrapper.model.Link;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private Mapper mapper;
    private RegisterChatRequest registerChatRequest;
    private AddLinkRequest addLinkRequest;
    private Chat chat;
    private Link link;

    @BeforeEach
    void setUp() {
        // arrange
        mapper = new Mapper();
        registerChatRequest =
            RegisterChatRequest.builder().chatId(123L).name("testUser").build();
        addLinkRequest = AddLinkRequest.builder()
            .uri("https://example.com")
            .tags(List.of("tag1", "tag2"))
            .filters(List.of("filter1", "filter2"))
            .build();
        chat = Chat.builder()
            .id(UUID.randomUUID())
            .chatId(123L)
            .username("testUser")
            .creationDate(LocalDateTime.now())
            .build();
        link = Link.builder()
            .id(UUID.randomUUID())
            .chatId(chat.id())
            .url("https://example.com")
            .tags(List.of("tag1", "tag2"))
            .filters(List.of("filter1", "filter2"))
            .lastUpdate(LocalDateTime.now())
            .build();
    }

    @Test
    void chatDtoToEntity_shouldMapRegisterChatRequestToChat() {
        // act
        Chat mappedChat = mapper.chatDtoToEntity(registerChatRequest);

        // assert
        assertThat(mappedChat)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("chatId", 123L)
            .hasFieldOrPropertyWithValue("username", "testUser")
            .hasFieldOrProperty("creationDate");
    }

    @Test
    void linkToLinkResponse_shouldMapLinkToLinkResponse() {
        // act
        LinkResponse mappedLinkResponse = mapper.linkToLinkResponse(link);

        // assert
        assertThat(mappedLinkResponse)
            .isNotNull()
            .hasFieldOrPropertyWithValue("id", link.id())
            .hasFieldOrPropertyWithValue("url", "https://example.com")
            .hasFieldOrPropertyWithValue("tags", List.of("tag1", "tag2"))
            .hasFieldOrPropertyWithValue("filters", List.of("filter1", "filter2"));
    }

    @Test
    void linkRequestToLink_shouldMapAddLinkRequestAndChatToLink() {
        // act
        Link mappedLink = mapper.linkRequestToLink(addLinkRequest, chat);

        // assert
        assertThat(mappedLink)
            .isNotNull()
            .hasFieldOrProperty("id")
            .hasFieldOrPropertyWithValue("chatId", chat.id())
            .hasFieldOrPropertyWithValue("url", "https://example.com")
            .hasFieldOrPropertyWithValue("tags", List.of("tag1", "tag2"))
            .hasFieldOrPropertyWithValue("filters", List.of("filter1", "filter2"))
            .hasFieldOrPropertyWithValue("lastUpdate", null);
    }
}
