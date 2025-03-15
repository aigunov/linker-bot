package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import java.util.Optional;
import java.util.UUID;

public interface FilterRepository {
    Filter save(Filter filter);
    void deleteById(UUID id);
    Optional<Filter> findById(UUID id);
    Iterable<Filter> findAll();
}
