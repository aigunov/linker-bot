package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "app.db", name="access-type", havingValue = "jpa")
public interface OrmLinkRepository extends LinkRepository, JpaRepository<Link, UUID> {
}
