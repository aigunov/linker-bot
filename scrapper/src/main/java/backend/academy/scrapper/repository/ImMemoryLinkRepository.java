package backend.academy.scrapper.repository;

import backend.academy.scrapper.model.Link;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Qualifier(value = "linkRepository")
@Component
public class ImMemoryLinkRepository implements Repository<Link>{
    private final Map<UUID, Link> storage = new ConcurrentHashMap<>();

    @Override
    public Link save(Link link) {
        storage.put(link.id(), link);
        link = storage.get(link.id());
        log.info("Link saved: {}", link);
        return link;
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


    public Optional<Link> findByChatIdAndLink(UUID chatId, String uri) {
        for (Link link : storage.values()) {
            if (link.chatId().equals(chatId) && link.url().equals(uri)) {
                return Optional.of(link);
            }
        }
        return Optional.empty();
    }
}
