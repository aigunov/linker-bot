package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.db", name = "access-type", havingValue = "sql")
public class SqlFilterRepository implements FilterRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Filter save(final Filter filter) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        var sql = """
            INSERT INTO filter (chat_id, parameter, value)
            VALUES (:chatId, :parameter, :value)
            ON CONFLICT (chat_id, parameter, value)
            DO UPDATE SET parameter = :parameter, value = :value
            RETURNING id
            """;
        var params = new MapSqlParameterSource()
                .addValue("chatId", filter.chat().id())
                .addValue("parameter", filter.parameter())
                .addValue("value", filter.value());

        jdbc.update(sql, params, keyHolder);
        filter.id((UUID) keyHolder.getKeys().get("id"));

        return filter;
    }

    @Transactional
    @Override
    public <S extends Filter> Iterable<S> saveAll(Iterable<S> filters) {
        var list = new ArrayList<S>();
        for (var filter: filters){
            list.add((S) save(filter));
        }
        return filters;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void deleteById(final UUID id) {
        var deleteLinkFilterSql =
                """
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

    @Override
    public Optional<Filter> findByTgIdAndFilter(final Long tgId, final String param, final String value) {
        var sql =
                """
            SELECT *
            FROM filter AS f
            JOIN chat AS c ON c.id = f.chat_id
            WHERE c.tg_id = :tgId AND f.parameter = :param AND f.value = :value
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tgId", tgId)
                .addValue("param", param)
                .addValue("value", value);
        List<Filter> results = jdbc.query(sql, params, new FilterResultSetExtractor());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    @Override
    public List<Filter> findAllByChatIdAndNotInLinkToFilterTable(final UUID chatId) {
        var sql =
                """
            SELECT f.*
            FROM filter as f
            WHERE f.chat_id = :chatId AND f.id NOT IN (SELECT filter_id
                                                       FROM link_to_filter)
            """;
        var params = new MapSqlParameterSource().addValue("chatId", chatId);
        return jdbc.query(sql, params, new FilterResultSetExtractor());
    }

    @Override
    public void deleteAll(Iterable<? extends Filter> filters) {
        var filterIds = ((List<Filter>) filters).stream().map(Filter::id).toList();

        var sql = """
            DELETE
            FROM filter
            WHERE id IN (:ids)
            """;

        jdbc.update(sql, new MapSqlParameterSource("ids", filterIds));
    }

    @Override
    public void deleteAll() {
        jdbc.update("DELETE FROM Filter", new MapSqlParameterSource());
    }

    @Override
    public List<Filter> findAll() {
        String sql = "SELECT * FROM filter";
        return jdbc.query(sql, new FilterResultSetExtractor());
    }
}
