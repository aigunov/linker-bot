package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface LinkRepository {
    Link save(Link link);

    void deleteAll();

    <S extends Link> List<S> saveAll(Iterable<S> links);

    void deleteById(UUID id);

    Optional<Link> findById(UUID id);

    Optional<Link> findByUrl(String url);

    Optional<Link> findByTgIdAndUrl(Long tgId, String url);

    Iterable<Link> findAll();

    Iterable<Link> findAll(Pageable pageable);

    Iterable<Link> findAllWithChats(Pageable pageable);

    Iterable<Link> findAllByTgId(Long tgId);

    Iterable<Link> findLinksByTgIdAndTags(Long tgId, List<String> tags);
}
