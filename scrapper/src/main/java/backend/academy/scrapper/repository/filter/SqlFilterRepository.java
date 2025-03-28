package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="sql")
public class SqlFilterRepository implements FilterRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Filter save(Filter filter) {
        return null;
    }

    @Override
    public void deleteById(UUID id) {

    }

    @Override
    public Optional<Filter> findById(UUID id) {
        return Optional.empty();
    }

    @Override
    public Optional<Filter> findByTgIdAndFilter(Long tgId, String filter) {
        return Optional.empty();
    }

    @Override
    public Iterable<Filter> findAll() {
        return null;
    }
}

