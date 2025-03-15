package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="sql")
public class SqlTagRepository implements TagRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public Tag save(Tag tag) {
        return null;
    }

    @Override
    public void deleteById(UUID id) {

    }

    @Override
    public Optional<Tag> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Iterable<Tag> findAll() {
        return null;
    }

}
