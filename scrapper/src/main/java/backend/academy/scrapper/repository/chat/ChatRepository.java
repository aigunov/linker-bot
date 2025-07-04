package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRepository {
    Chat save(Chat chat);

    void deleteById(UUID id);

    void deleteByTgId(Long tgId);

    void deleteAll();

    <S extends Chat> List<S> saveAll(Iterable<S> chats);

    Optional<Chat> findById(UUID id);

    Optional<Chat> findByTgId(Long tgId);

    Iterable<Chat> findAll();

    void setDigestTime(Long chatId, LocalTime time);
}
