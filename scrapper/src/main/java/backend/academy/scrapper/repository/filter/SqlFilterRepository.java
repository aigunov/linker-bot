package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix="app.db", name="access-type", havingValue="sql")
public class SqlFilterRepository implements FilterRepository {
    private final NamedParameterJdbcTemplate jdbc;

    //todo: Returning id?
    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Filter save(Filter filter) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        var sql = """
            INSERT INTO filter (chat_id, parameter, value)
            VALUES (:chatId, :parameter, :value)
            """;
        var params = new MapSqlParameterSource()
            .addValue("chatId", filter.chat().id())
            .addValue("parameter", filter.parameter())
            .addValue("value", filter.value());

        jdbc.update(sql, params, keyHolder);
        filter.id((UUID) keyHolder.getKeys().get("id"));

        var linkFilterSql = """
            INSERT INTO link_to_filter (filter_id, link_id)
            VALUES (:filterId, :linkId)
            """;
        filter.links().forEach(link -> {
            MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("filterId", filter.id().toString())
                .addValue("linkId", link.id().toString());
            jdbc.update(linkFilterSql, param);
        });
        return filter;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void deleteById(UUID id) {
        var deleteLinkFilterSql = """
            DELETE
            FROM link_to_filter
            WHERE filter_id = :filterId
            """;

        jdbc.update(deleteLinkFilterSql, new MapSqlParameterSource("filterId", id));

        var sql = """
            DELETE
            FROM filter
            WHERE id = :id
            """;

        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Filter> findById(final UUID id) {
        var sql = """
            SELECT *
            FROM filter
            WHERE id = :id
            """;

        var result = jdbc.query(sql, new MapSqlParameterSource("id", id), new FilterResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    //TODO: Должна быть проверка по двумя полям таблицы filter
    @Override
    public Optional<Filter> findByTgIdAndFilter(Long tgId, String filter) {
        var sql = """
            SELECT *
            FROM filter AS f
            JOIN chat AS c ON c.id = f.chat_id
            WHERE c.tg_id = :tgId AND f.parameter = :filterParam
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("tgId", tgId)
            .addValue("filterParam", filter);
        List<Filter> results = jdbc.query(sql, params, new FilterResultSetExtractor());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<Filter> findAll() {
        String sql = "SELECT * FROM filter";
        return jdbc.query(sql, new FilterResultSetExtractor());
    }
}

