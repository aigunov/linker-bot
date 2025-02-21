package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@org.springframework.stereotype.Repository
public class ImMemoryLinkRepository implements Repository<Link>{
    private final Map<UUID, Link> storage = new ConcurrentHashMap<>();

    @Override
    public Link save(Link link) {
        return storage.put(link.id(), link);
    }

    @Override
    public Optional<Link> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Link> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Link deleteById(UUID id) {
        return storage.remove(id);
    }
}
