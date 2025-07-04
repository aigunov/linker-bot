package backend.academy.bot.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dto.LinkResponse;
import dto.ListLinkResponse;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListStateTest {

    private final ListState listState = new ListState();

    @Test
    void formatLinks_shouldReturnFormattedString() {
        // arrange
        ListLinkResponse links = ListLinkResponse.builder()
                .linkResponses(List.of(
                        LinkResponse.builder()
                                .url("https://example.com")
                                .tags(List.of("tag1", "tag2"))
                                .filters(List.of())
                                .build(),
                        LinkResponse.builder()
                                .url("https://test.com")
                                .filters(List.of("filter1"))
                                .tags(List.of())
                                .build()))
                .build();

        String expected =
                """
                🔗 <b>Отслеживаемые ссылки:</b>

                🌐 <b>URL:</b> https://example.com
                🏷 <b>Теги:</b> tag1, tag2
                🔍 <i>Фильтры отсутствуют</i>

                🌐 <b>URL:</b> https://test.com
                🏷 <i>Теги отсутствуют</i>
                🔍 <b>Фильтры:</b> filter1

                """;

        // act
        String result = listState.formatLinks(links);

        // assert
        assertEquals(expected, result);
    }

    @Test
    void formatLinks_shouldReturnNoLinksMessage_whenEmptyList() {
        // arrange
        ListLinkResponse links =
                ListLinkResponse.builder().linkResponses(List.of()).build();

        // act
        String result = listState.formatLinks(links);

        // assert
        assertEquals("Вы пока не добавили ни одну ссылку для отслеживания", result);
    }

    @Test
    void formatLinks_shouldHandleEmptyTagsAndFilters() {
        // arrange
        ListLinkResponse links = ListLinkResponse.builder()
                .linkResponses(List.of(LinkResponse.builder()
                        .url("https://example.com")
                        .tags(List.of())
                        .filters(List.of())
                        .build()))
                .build();

        String expected =
                """
                🔗 <b>Отслеживаемые ссылки:</b>

                🌐 <b>URL:</b> https://example.com
                🏷 <i>Теги отсутствуют</i>
                🔍 <i>Фильтры отсутствуют</i>

                """;

        // act
        String result = listState.formatLinks(links);

        // assert
        assertEquals(expected, result);
    }
}
