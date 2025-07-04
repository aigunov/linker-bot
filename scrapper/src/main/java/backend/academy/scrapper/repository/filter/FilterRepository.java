package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import java.util.Optional;
import java.util.UUID;

public interface FilterRepository {
    Filter save(Filter filter);

    <S extends Filter> Iterable<S> saveAll(Iterable<S> filters);

    void deleteById(UUID id);

    void deleteAll(Iterable<? extends Filter> filters);

    void deleteAll();

    Optional<Filter> findById(UUID id);

    Optional<Filter> findByTgIdAndFilter(Long tgId, String param, String value);

    Iterable<Filter> findAllByChatIdAndNotInLinkToFilterTable(UUID id);

    Iterable<Filter> findAll();
}
