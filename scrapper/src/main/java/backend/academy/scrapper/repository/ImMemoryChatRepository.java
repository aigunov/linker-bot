package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Chat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Repository
public class ImMemoryChatRepository implements Repository<Chat>{
    private final Map<UUID, Chat> storage = new ConcurrentHashMap<>();

    @Override
    public Chat save(Chat chat) {
        return storage.put(chat.id(), chat);
    }

    @Override
    public Optional<Chat> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
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
