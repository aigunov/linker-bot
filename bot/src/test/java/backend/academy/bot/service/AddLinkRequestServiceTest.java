package backend.academy.bot.service;

class AddLinkRequestServiceTest {

    //    private AddLinkRequestService service;
    //    private Long chatId;
    //
    //    @BeforeEach
    //    void setUp() {
    //        // arrange
    //        service = new AddLinkRequestService();
    //        chatId = 123L;
    //    }
    //
    //    @Test
    //    void createLinkRequest_shouldCreateNewRequest() {
    //        // act
    //        service.createLinkRequest(chatId, "https://example.com");
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertThat(request).isNotNull();
    //        assertThat(request.uri()).isEqualTo("https://example.com");
    //        assertThat(request.tags()).isNullOrEmpty();
    //        assertThat(request.filters()).isNullOrEmpty();
    //    }
    //
    //    @Test
    //    void getLinkRequest_shouldReturnExistingRequest() {
    //        // arrange
    //        service.createLinkRequest(chatId, "https://example.com");
    //
    //        // act
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertThat(request).isNotNull();
    //    }
    //
    //    @Test
    //    void getLinkRequest_shouldReturnNullForNonExistentChatId() {
    //        // act
    //        AddLinkRequest request = service.getLinkRequest(456L);
    //
    //        // assert
    //        assertNull(request);
    //    }
    //
    //    @Test
    //    void updateLinkRequestTags_shouldUpdateTags() {
    //        // arrange
    //        service.createLinkRequest(chatId, "https://example.com");
    //
    //        // act
    //        service.updateLinkRequestTags(chatId, "tag1 tag2");
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertThat(request).isNotNull();
    //        assertThat(request.tags()).containsExactly("tag1", "tag2");
    //    }
    //
    //    @Test
    //    void updateLinkRequestTags_shouldNotUpdateTagsForNonExistentChatId() {
    //        // act
    //        service.updateLinkRequestTags(456L, "tag1 tag2");
    //        AddLinkRequest request = service.getLinkRequest(456L);
    //
    //        // assert
    //        assertNull(request);
    //    }
    //
    //    @Test
    //    void updateLinkRequestFilters_shouldUpdateFilters() {
    //        // arrange
    //        service.createLinkRequest(chatId, "https://example.com");
    //
    //        // act
    //        service.updateLinkRequestFilters(chatId, "filter1 filter2");
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertThat(request).isNotNull();
    //        assertThat(request.filters()).containsExactly("filter1", "filter2");
    //    }
    //
    //    @Test
    //    void updateLinkRequestFilters_shouldNotUpdateFiltersForNonExistentChatId() {
    //        // act
    //        service.updateLinkRequestFilters(456L, "filter1 filter2");
    //        AddLinkRequest request = service.getLinkRequest(456L);
    //
    //        // assert
    //        assertNull(request);
    //    }
    //
    //    @Test
    //    void clearLinkRequest_shouldRemoveRequest() {
    //        // arrange
    //        service.createLinkRequest(chatId, "https://example.com");
    //
    //        // act
    //        service.clearLinkRequest(chatId);
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertNull(request);
    //    }
    //
    //    @Test
    //    void clearLinkRequest_shouldNotFailForNonExistentChatId() {
    //        // act
    //        service.clearLinkRequest(456L);
    //        AddLinkRequest request = service.getLinkRequest(456L);
    //
    //        // assert
    //        assertNull(request);
    //    }
    //
    //    @Test
    //    void updateLinkRequestTags_shouldAddTagsToExisting() {
    //        // arrange
    //        service.createLinkRequest(chatId, "https://example.com");
    //        service.updateLinkRequestTags(chatId, "tag1");
    //
    //        // act
    //        service.updateLinkRequestTags(chatId, "tag2");
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertThat(request).isNotNull();
    //        assertThat(request.tags()).containsExactly("tag1", "tag2");
    //    }
    //
    //    @Test
    //    void updateLinkRequestFilters_shouldAddFiltersToExisting() {
    //        // arrange
    //        service.createLinkRequest(chatId, "https://example.com");
    //        service.updateLinkRequestFilters(chatId, "filter1");
    //
    //        // act
    //        service.updateLinkRequestFilters(chatId, "filter2");
    //        AddLinkRequest request = service.getLinkRequest(chatId);
    //
    //        // assert
    //        assertThat(request).isNotNull();
    //        assertThat(request.filters()).containsExactly("filter1", "filter2");
    //    }
}
