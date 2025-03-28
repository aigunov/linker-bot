package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
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
public class SqlTagRepository implements TagRepository {
    private final NamedParameterJdbcTemplate jdbc;


    //todo: returning id?
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Tag save(Tag tag) {
        var tagSql = """
            INSERT INTO tag(chat_id, tag)
            VALUES (:chatId, :tag)
            """;
        var tagParams = new MapSqlParameterSource()
            .addValue("chatId", tag.chat().id().toString())
            .addValue("tag", tag.tag());
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(tagSql, tagParams, keyHolder);
        tag.id((UUID) keyHolder.getKeys().get("id"));

        var linkTagSql = """
            INSERT INTO tag_to_link(link_id, tag_id)
            VALUES (:linkId, :tagId)
            """;

        tag.links().forEach(link -> {
            MapSqlParameterSource linkTagParams = new MapSqlParameterSource()
                .addValue("linkId", link.id().toString())
                .addValue("tagId", tag.id().toString());
            jdbc.update(linkTagSql, linkTagParams);
        });
        return tag;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteById(UUID id) {
        var deleteLinkTagSql = """
            DELETE FROM tag_to_link
            WHERE tag_id = :tagId
            """;
        jdbc.update(deleteLinkTagSql, new MapSqlParameterSource("tagId", id.toString()));

        var deleteTagSql = """
            DELETE FROM tag
            WHERE id = :id
            """;
        jdbc.update(deleteTagSql, new MapSqlParameterSource("id", id.toString()));
    }

    @Override
    public Optional<Tag> findById(UUID id) {
        var sql = """
            SELECT *
            FROM tag
            WHERE id = :id
            """;
        var result = jdbc.query(sql, new MapSqlParameterSource("id", id.toString()), new TagResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public Optional<Tag> findByTgIdAndTag(final Long tgId, final String tag) {
        var sql = """
            SELECT t.*
            FROM tag AS t
            JOIN chat AS c ON t.chat_id = c.id
            WHERE c.tg_id = :tgId AND t.tag = :tag
            """;
        var params = new MapSqlParameterSource()
            .addValue("tgId", tgId)
            .addValue("tag", tag);
        var result = jdbc.query(sql, params, new TagResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public List<Tag> findAllByTgId(final Long tgId) {
        var sql = """
            SELECT t.*
            FROM tag AS t
            JOIN chat AS c ON t.chat_id = c.id
            WHERE c.tg_id = :chatId
            """;
        var result = jdbc.query(sql,
            new MapSqlParameterSource("tg_id", tgId), new TagResultSetExtractor());
        return result;
    }

    @Override
    public List<Tag> findAll() {
        var sql = "SELECT * FROM tag";
        return jdbc.query(sql, new TagResultSetExtractor());
    }

}
