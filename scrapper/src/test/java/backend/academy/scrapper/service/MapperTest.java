package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import dto.AddLinkRequest;
import dto.LinkResponse;
import dto.RegisterChatRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MapperTest {

    private Mapper mapper;
    private RegisterChatRequest registerChatRequest;
    private AddLinkRequest addLinkRequest;
    private Chat chat;
    private Link link;

    @BeforeEach
    void setUp() {
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
        LinkResponse mappedLinkResponse = null;

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
        Link mappedLink = null;

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
