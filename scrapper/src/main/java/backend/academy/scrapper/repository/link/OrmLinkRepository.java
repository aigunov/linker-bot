package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.db", name="access-type", havingValue = "jpa")
public interface OrmLinkRepository extends LinkRepository, JpaRepository<Link, UUID> {

    @Query("""
        SELECT l
        FROM Link l
        JOIN l.chats c
        WHERE c.tgId = :tgId AND l.url = :url
        """)
    @Override
    Optional<Link> findByTgIdAndUrl(@Param("tgId") Long tgId, @Param("url") String url);

    @Query("""
        SELECT l
        FROM Link l
        JOIN l.chats c
        JOIN l.tags t
        WHERE c.tgId = :tgId AND t.tag IN :tags
        """)
    @Override
    List<Link> findLinksByTgIdAndTags(@Param("tgId") Long tgId, @Param("tags") List<String> tags, @Param("size") Long size);

    @Query("""
        SELECT l
        FROM Link l
        JOIN l.chats c
        WHERE c.tgId = :tgId""")
    @Override
    List<Link> findAllByTgId(@Param("tgId") Long tgId);
}
