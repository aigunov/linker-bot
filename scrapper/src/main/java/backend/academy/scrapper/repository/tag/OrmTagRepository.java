package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="jpa")
public interface OrmTagRepository extends TagRepository, JpaRepository<Tag, UUID> {

    @Query(value = """
        SELECT t.*
        FROM tag AS t
        JOIN chat AS c ON t.chat_id = c.id
        WHERE c.tg_id = :tgId
        """, nativeQuery = true)
    @Override
    List<Tag> findAllByTgId(final @Param("userId") Long chatId);


    @Query(value = """
        SELECT t.*
        FROM tag AS t
        JOIN chat AS c ON t.chat_id = c.id
        WHERE c.tg_id = :tgId AND t.tag = :tag
        """, nativeQuery = true)
    @Override
    Optional<Tag> findByTgIdAndTag(final @Param("tgId") Long tgId, final @Param("tag") String tag);

}
