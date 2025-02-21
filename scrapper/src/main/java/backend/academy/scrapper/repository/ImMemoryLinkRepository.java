package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Qualifier(value = "linkRepository")
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

    public List<Link> findAllByChatId(UUID chatId) {
        final List<Link> links = new ArrayList<>();
        for (Link link : storage.values()) {
            if(link.chatId().equals(chatId)){
                links.add(link);
            }
        }
        return links;
    }

    @Override
    public Link deleteById(UUID id) {
        return storage.remove(id);
    }

    public Optional<Link> deleteByChatIdAndLinkUrl(UUID id, String uri) {
        for (Link link : storage.values()) {
            if (link.url().equals(uri) && link.chatId().equals(id)) {
                return Optional.of(storage.remove(link.id()));
            }
        }
        return Optional.empty();
    }
}
