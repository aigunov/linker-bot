package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="app.db", name = "access-type", havingValue="sql")
public class SqlLinkRepository implements LinkRepository{
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Link save(Link link) {
        return null;
    }

    @Override
    public void deleteById(UUID id) {

    }

    @Override
    public Optional<Link> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<Link> findByTgIdAndUrl(Long chatId, String url) {
        return Optional.empty();
    }

    @Override
    public Iterable<Link> findAll() {
        return null;
    }

    @Override
    public Iterable<Link> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public Iterable<Link> findAllByTgId(Long chatId) {
        return null;
    }

    @Override
    public Iterable<Link> findLinksByTgIdAndTags(Long chatId, List<String> tags, long size) {
        return null;
    }
}
