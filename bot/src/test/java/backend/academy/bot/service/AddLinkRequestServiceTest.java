package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.*;

import dto.AddLinkRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class AddLinkRequestServiceTest {

    private AddLinkRequestService service;
    private Long chatId;

    @BeforeEach
    void setUp() {
        service = new AddLinkRequestService();
        chatId = 123L;
    }

    @Test
    void createLinkRequest_shouldCreateNewRequest() {
        service.createLinkRequest(chatId, "https://example.com");
        AddLinkRequest request = service.getLinkRequest(chatId);

        assertThat(request).isNotNull();
        assertThat(request.uri()).isEqualTo("https://example.com");
        assertThat(request.tags()).isNullOrEmpty();
        assertThat(request.filters()).isNullOrEmpty();
    }

    @Test
    void getLinkRequest_shouldReturnExistingRequest() {
        service.createLinkRequest(chatId, "https://example.com");
        AddLinkRequest request = service.getLinkRequest(chatId);
        assertThat(request).isNotNull();
    }

    @Test
    void getLinkRequest_shouldReturnNullForNonExistentChatId() {
        AddLinkRequest request = service.getLinkRequest(456L);
        assertNull(request);
    }

    @Test
    void updateLinkRequestTags_shouldUpdateTags() {
        service.createLinkRequest(chatId, "https://example.com");
        service.updateLinkRequestTags(chatId, "tag1 tag2");
        AddLinkRequest request = service.getLinkRequest(chatId);

        assertThat(request).isNotNull();
        assertThat(request.tags()).containsExactly("tag1", "tag2");
    }

    @Test
    void updateLinkRequestTags_shouldNotUpdateTagsForNonExistentChatId() {
        service.updateLinkRequestTags(456L, "tag1 tag2");
        AddLinkRequest request = service.getLinkRequest(456L);
        assertNull(request);
    }

    @Test
    void updateLinkRequestFilters_shouldUpdateFilters() {
        service.createLinkRequest(chatId, "https://example.com");
        service.updateLinkRequestFilters(chatId, "filter1 filter2");
        AddLinkRequest request = service.getLinkRequest(chatId);

        assertThat(request).isNotNull();
        assertThat(request.filters()).containsExactly("filter1", "filter2");
    }

    @Test
    void updateLinkRequestFilters_shouldNotUpdateFiltersForNonExistentChatId() {
        service.updateLinkRequestFilters(456L, "filter1 filter2");
        AddLinkRequest request = service.getLinkRequest(456L);
        assertNull(request);
    }

    @Test
    void clearLinkRequest_shouldRemoveRequest() {
        service.createLinkRequest(chatId, "https://example.com");
        service.clearLinkRequest(chatId);
        AddLinkRequest request = service.getLinkRequest(chatId);
        assertNull(request);
    }

    @Test
    void clearLinkRequest_shouldNotFailForNonExistentChatId() {
        service.clearLinkRequest(456L);
        AddLinkRequest request = service.getLinkRequest(456L);
        assertNull(request);
    }

    @Test
    void updateLinkRequestTags_shouldAddTagsToExisting() {
        service.createLinkRequest(chatId, "https://example.com");
        service.updateLinkRequestTags(chatId, "tag1");
        service.updateLinkRequestTags(chatId, "tag2");
        AddLinkRequest request = service.getLinkRequest(chatId);

        assertThat(request).isNotNull();
        assertThat(request.tags()).containsExactly("tag1", "tag2");
    }

    @Test
    void updateLinkRequestFilters_shouldAddFiltersToExisting() {
        service.createLinkRequest(chatId, "https://example.com");
        service.updateLinkRequestFilters(chatId, "filter1");
        service.updateLinkRequestFilters(chatId, "filter2");
        AddLinkRequest request = service.getLinkRequest(chatId);

        assertThat(request).isNotNull();
        assertThat(request.filters()).containsExactly("filter1", "filter2");
    }
}
