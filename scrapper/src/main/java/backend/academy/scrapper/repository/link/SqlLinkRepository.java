package backend.academy.scrapper.repository.link;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix="app", name = "access-type", havingValue="sql")
public class SqlLinkRepository implements LinkRepository{
}
