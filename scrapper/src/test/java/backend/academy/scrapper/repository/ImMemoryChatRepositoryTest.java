package backend.academy.scrapper.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.scrapper.model.Chat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryChatRepositoryTest {

    private InMemoryChatRepository repository;
    private Chat chat1;
    private Chat chat2;

    @BeforeEach
    void setUp() {
        // arrange
        repository = new InMemoryChatRepository();
        chat1 = Chat.builder()
                .id(UUID.randomUUID())
                .chatId(123L)
                .username("user1")
                .creationDate(LocalDateTime.now())
                .build();
        chat2 = Chat.builder()
                .id(UUID.randomUUID())
                .chatId(456L)
                .username("user2")
                .creationDate(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void save_shouldSaveChat() {
        // act
        Chat savedChat = repository.save(chat1);

        // assert
        assertEquals(chat1, savedChat);
        Optional<Chat> foundChat = repository.findById(chat1.id());
        assertTrue(foundChat.isPresent());
        assertEquals(chat1, foundChat.get());
    }

    @Test
    void findById_shouldReturnChat_whenChatExists() {
        // arrange
        repository.save(chat1);

        // act
        Optional<Chat> foundChat = repository.findById(chat1.id());

        // assert
        assertTrue(foundChat.isPresent());
        assertEquals(chat1, foundChat.get());
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenChatDoesNotExist() {
        // act
        Optional<Chat> foundChat = repository.findById(UUID.randomUUID());

        // assert
        assertFalse(foundChat.isPresent());
    }

    @Test
    void findByChatId_shouldReturnChat_whenChatExists() {
        // arrange
        repository.save(chat1);

        // act
        Optional<Chat> foundChat = repository.findByChatId(123L);

        // assert
        assertTrue(foundChat.isPresent());
        assertEquals(chat1, foundChat.get());
    }

    @Test
    void findByChatId_shouldReturnEmptyOptional_whenChatDoesNotExist() {
        // act
        Optional<Chat> foundChat = repository.findByChatId(789L);

        // assert
        assertFalse(foundChat.isPresent());
    }

    @Test
    void findAll_shouldReturnAllChats() {
        // arrange
        repository.save(chat1);
        repository.save(chat2);

        // act
        List<Chat> allChats = repository.findAll();

        // assert
        assertEquals(2, allChats.size());
        assertTrue(allChats.contains(chat1));
        assertTrue(allChats.contains(chat2));
    }

    @Test
    void deleteById_shouldRemoveChat_whenChatExists() {
        // arrange
        repository.save(chat1);

        // act
        Chat deletedChat = repository.deleteById(chat1.id());

        // assert
        assertEquals(chat1, deletedChat);
        Optional<Chat> foundChat = repository.findById(chat1.id());
        assertFalse(foundChat.isPresent());
    }

    @Test
    void deleteById_shouldReturnNull_whenChatDoesNotExist() {
        // act
        Chat deletedChat = repository.deleteById(UUID.randomUUID());

        // assert
        assertNull(deletedChat);
    }
}
