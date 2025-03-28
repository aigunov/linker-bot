package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository {
    Chat save(Chat chat);
    void deleteById(UUID id);
    void deleteByTgId(Long tgId);
    Optional<Chat> findById(UUID id);
    Optional<Chat> findByTgId(Long tgId);
    Iterable<Chat> findAll();
}
