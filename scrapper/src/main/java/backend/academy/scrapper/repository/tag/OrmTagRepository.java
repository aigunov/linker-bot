package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="jpa")
public interface OrmTagRepository extends TagRepository, JpaRepository<Tag, UUID> {
}
