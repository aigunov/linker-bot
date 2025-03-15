package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryLinkRepositoryTest {

    private InMemoryLinkRepository repository;
    private Link link1;
    private Link link2;
    private UUID chatId1;
    private UUID chatId2;

    @BeforeEach
    void setUp() {
        // arrange
        repository = new InMemoryLinkRepository();
        chatId1 = UUID.randomUUID();
        chatId2 = UUID.randomUUID();
        link1 = Link.builder()
            .id(UUID.randomUUID())
            .chatId(chatId1)
            .url("https://example.com/1")
            .lastUpdate(LocalDateTime.now())
            .build();
        link2 = Link.builder()
            .id(UUID.randomUUID())
            .chatId(chatId2)
            .url("https://example.com/2")
            .lastUpdate(LocalDateTime.now().plusHours(1))
            .build();
    }

    @Test
    void save_shouldSaveLink() {
        // act
        Link savedLink = repository.save(link1);

        // assert
        assertEquals(link1, savedLink);
        Optional<Link> foundLink = repository.findById(link1.id());
        assertTrue(foundLink.isPresent());
        assertEquals(link1, foundLink.get());
    }

    @Test
    void findById_shouldReturnLink_whenLinkExists() {
        // arrange
        repository.save(link1);

        // act
        Optional<Link> foundLink = repository.findById(link1.id());

        // assert
        assertTrue(foundLink.isPresent());
        assertEquals(link1, foundLink.get());
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenLinkDoesNotExist() {
        // act
        Optional<Link> foundLink = repository.findById(UUID.randomUUID());

        // assert
        assertFalse(foundLink.isPresent());
    }

    @Test
    void findAll_shouldReturnAllLinks() {
        // arrange
        repository.save(link1);
        repository.save(link2);

        // act
        List<Link> allLinks = repository.findAll();

        // assert
        assertEquals(2, allLinks.size());
        assertTrue(allLinks.contains(link1));
        assertTrue(allLinks.contains(link2));
    }

    @Test
    void findAllByChatId_shouldReturnLinksForChatId() {
        // arrange
        repository.save(link1);
        repository.save(link2);

        // act
        List<Link> linksForChat1 = repository.findAllByChatId(chatId1);
        List<Link> linksForChat2 = repository.findAllByChatId(chatId2);

        // assert
        assertEquals(1, linksForChat1.size());
        assertTrue(linksForChat1.contains(link1));
        assertEquals(1, linksForChat2.size());
        assertTrue(linksForChat2.contains(link2));
    }

    @Test
    void deleteById_shouldRemoveLink_whenLinkExists() {
        // arrange
        repository.save(link1);

        // act
        Link deletedLink = repository.deleteById(link1.id());

        // assert
        assertEquals(link1, deletedLink);
        Optional<Link> foundLink = repository.findById(link1.id());
        assertFalse(foundLink.isPresent());
    }

    @Test
    void deleteById_shouldReturnNull_whenLinkDoesNotExist() {
        // act
        Link deletedLink = repository.deleteById(UUID.randomUUID());

        // assert
        assertNull(deletedLink);
    }

    @Test
    void findByChatIdAndLink_shouldReturnLink_whenLinkExists() {
        // arrange
        repository.save(link1);

        // act
        Optional<Link> foundLink = repository.findByChatIdAndLink(chatId1, "https://example.com/1");

        // assert
        assertTrue(foundLink.isPresent());
        assertEquals(link1, foundLink.get());
    }

    @Test
    void findByChatIdAndLink_shouldReturnEmptyOptional_whenLinkDoesNotExist() {
        // act
        Optional<Link> foundLink = repository.findByChatIdAndLink(chatId1, "https://example.com/nonexistent");

        // assert
        assertFalse(foundLink.isPresent());
    }
}
