package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
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
        """, nativeQuery = true)
    @Override
    Iterable<Tag> findAllByTgId(@Param("userId") Long chatId);
}
