package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "app.db", name = "access-type", havingValue = "orm")
public interface OrmFilterRepository extends FilterRepository, JpaRepository<Filter, UUID> {

    @Query(
            """
        SELECT f
        FROM Filter f
        WHERE f.chat.tgId = :tgId AND
            f.parameter = :param AND
            f.value = :value
        """)
    @Override
    Optional<Filter> findByTgIdAndFilter(
            final @Param("tgId") Long tgId, final @Param("param") String param, final @Param("value") String value);

    @Query(
            value =
                    """
        SELECT f.*
        FROM filter as f
        WHERE f.chat_id = :chatId AND f.id NOT IN (SELECT filter_id
                                                   FROM link_to_filter)
        """,
            nativeQuery = true)
    @Override
    List<Filter> findAllByChatIdAndNotInLinkToFilterTable(final @Param("chatId") UUID chatId);
}
