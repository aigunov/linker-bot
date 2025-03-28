package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {
    Link save(Link link);
    void deleteById(UUID id);
    Optional<Link> findById(UUID id);
    Optional<Link> findByTgIdAndUrl(Long chatId, String url);
    Iterable<Link> findAll();
    Iterable<Link> findAll(Pageable pageable);
    Iterable<Link> findAllByTgId(Long chatId);
    Iterable<Link> findLinksByTgIdAndTags(Long chatId, List<String> tags, long size);
}
