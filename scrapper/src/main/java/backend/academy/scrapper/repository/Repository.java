package backend.academy.scrapper.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Repository<T> {
    T save(T item);

    List<T> findAll();

    Optional<T> findById(UUID id);

    T deleteById(UUID id);
}
