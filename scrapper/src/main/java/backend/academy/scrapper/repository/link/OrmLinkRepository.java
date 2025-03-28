package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.db", name="access-type", havingValue = "jpa")
public interface OrmLinkRepository extends LinkRepository, JpaRepository<Link, UUID> {

    @Query(value = """
        """, nativeQuery = true)
    @Override
    Iterable<Link> findAllByTgId(Long chatId);

    @Override
    @Query(value = """
        """, nativeQuery = true)
    Optional<Link> findByTgIdAndUrl(Long chatId, String url);

    @Override
    Page<Link> findAll(Pageable pageable);
}
