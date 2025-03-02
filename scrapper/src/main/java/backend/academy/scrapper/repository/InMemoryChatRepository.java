package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Chat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Qualifier(value = "chatRepository")
@Component
public class InMemoryChatRepository implements Repository<Chat> {
    private final Map<UUID, Chat> storage = new ConcurrentHashMap<>();

    @Override
    public Chat save(Chat chat) {
        storage.put(chat.id(), chat);
        chat = storage.get(chat.id());
        log.info("Зарегистрирован новый чат: {}", chat.id());
        return chat;
    }

    @Override
    public Optional<Chat> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Chat> findByChatId(Long chatId) {
        for (Chat chat : storage.values()) {
            if (chat.chatId().equals(chatId)) {
                return Optional.of(chat);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Chat> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Chat deleteById(UUID id) {
        return storage.remove(id);
    }
}
