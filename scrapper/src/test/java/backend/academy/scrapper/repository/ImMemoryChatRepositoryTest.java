package backend.academy.scrapper.repository;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.data.model.Chat;
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
        repository = new InMemoryChatRepository();
        chat1 = Chat.builder()
                .id(UUID.randomUUID())
                .tgId(123L)
                .nickname("user1")
                .creationDate(LocalDateTime.now())
                .build();
        chat2 = Chat.builder()
                .id(UUID.randomUUID())
                .tgId(456L)
                .nickname("user2")
                .creationDate(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void save_shouldSaveChat() {
        Chat savedChat = repository.save(chat1);
        assertEquals(chat1, savedChat);
        Optional<Chat> foundChat = repository.findById(chat1.id());
        assertTrue(foundChat.isPresent());
        assertEquals(chat1, foundChat.get());
    }

    @Test
    void findById_shouldReturnChat_whenChatExists() {
        repository.save(chat1);
        Optional<Chat> foundChat = repository.findById(chat1.id());
        assertTrue(foundChat.isPresent());
        assertEquals(chat1, foundChat.get());
    }

    @Test
    void findById_shouldReturnEmptyOptional_whenChatDoesNotExist() {
        Optional<Chat> foundChat = repository.findById(UUID.randomUUID());
        assertFalse(foundChat.isPresent());
    }

    @Test
    void findByChatId_shouldReturnChat_whenChatExists() {
        repository.save(chat1);
        Optional<Chat> foundChat = repository.findByChatId(123L);
        assertTrue(foundChat.isPresent());
        assertEquals(chat1, foundChat.get());
    }

    @Test
    void findByChatId_shouldReturnEmptyOptional_whenChatDoesNotExist() {
        Optional<Chat> foundChat = repository.findByChatId(789L);
        assertFalse(foundChat.isPresent());
    }

    @Test
    void findAll_shouldReturnAllChats() {
        repository.save(chat1);
        repository.save(chat2);
        List<Chat> allChats = repository.findAll();
        assertEquals(2, allChats.size());
        assertTrue(allChats.contains(chat1));
        assertTrue(allChats.contains(chat2));
    }

    @Test
    void deleteById_shouldRemoveChat_whenChatExists() {
        repository.save(chat1);
        Chat deletedChat = repository.deleteById(chat1.id());
        assertEquals(chat1, deletedChat);
        Optional<Chat> foundChat = repository.findById(chat1.id());
        assertFalse(foundChat.isPresent());
    }

    @Test
    void deleteById_shouldReturnNull_whenChatDoesNotExist() {
        Chat deletedChat = repository.deleteById(UUID.randomUUID());
        assertNull(deletedChat);
    }
}
