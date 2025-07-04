package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import backend.academy.scrapper.exception.SqlRepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class SqlTagRepository implements TagRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Tag save(Tag tag) {
        String tagSql =
                """
            INSERT INTO tag(chat_id, tag)
            VALUES (:chatId, :tag)
            ON CONFLICT (chat_id, tag)
            DO UPDATE SET tag = EXCLUDED.tag
            RETURNING id
            """;

        var tagParams =
                new MapSqlParameterSource().addValue("chatId", tag.chat().id()).addValue("tag", tag.tag());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(tagSql, tagParams, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            Object idValue = keys.get("id");
            if (idValue instanceof UUID uuid) {
                tag.id(uuid);
            } else {
                throw new SqlRepositoryException("Generated ID 'id' is not a UUID or is null for tag");
            }
        } else {
            throw new SqlRepositoryException("Failed to retrieve generated ID for tag");
        }

        return tag;
    }

    @Transactional
    @Override
    public <S extends Tag> Iterable<S> saveAll(Iterable<S> tags) {
        var list = new ArrayList<S>();
        for (var tag : tags) {
            list.add((S) save(tag));
        }
        return list;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteById(UUID id) {
        var deleteLinkTagSql =
                """
                DELETE FROM tag_to_link
                WHERE tag_id = :tagId
                """;
        jdbc.update(deleteLinkTagSql, new MapSqlParameterSource("tagId", id));

        var deleteTagSql = """
            DELETE FROM tag
            WHERE id = :id
            """;
        jdbc.update(deleteTagSql, new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Tag> findById(UUID id) {
        var sql = """
            SELECT *
            FROM tag
            WHERE id = :id
            """;
        var result = jdbc.query(sql, new MapSqlParameterSource("id", id), new TagResultSetExtractor());
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().findFirst();
    }

    @Override
    public Optional<Tag> findByTgIdAndTag(final Long tgId, final String tag) {
        var sql =
                """
                SELECT t.*
                FROM tag AS t
                JOIN chat AS c ON t.chat_id = c.id
                WHERE c.tg_id = :tgId AND t.tag = :tag
                """;
        var params = new MapSqlParameterSource().addValue("tgId", tgId).addValue("tag", tag);
        var result = jdbc.query(sql, params, new TagResultSetExtractor());
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().findFirst();
    }

    @Override
    public List<Tag> findAllByChatIdAndNotInTagToLinkTable(final UUID chatId) {
        var sql =
                """
                SELECT *
                FROM tag
                WHERE chat_id = :chatId AND id NOT IN (SELECT tag_id
                                                       FROM tag_to_link)
                """;
        var params = new MapSqlParameterSource().addValue("chatId", chatId);
        return jdbc.query(sql, params, new TagResultSetExtractor());
    }

    @Override
    public void deleteAll(Iterable<? extends Tag> tags) {
        var tagIds = ((List<Tag>) tags).stream().map(Tag::id).toList();

        var sql = """
            DELETE
            FROM tag
            WHERE id IN (:ids)
            """;

        jdbc.update(sql, new MapSqlParameterSource("ids", tagIds));
    }

    @Override
    public void deleteAll() {
        jdbc.update("DELETE FROM Tag", new MapSqlParameterSource());
    }

    @Override
    public List<Tag> findAllByTgId(final Long tgId) {
        var sql =
                """
                SELECT t.*
                FROM tag AS t
                JOIN chat AS c ON t.chat_id = c.id
                WHERE c.tg_id = :tgId
                """;
        return jdbc.query(sql, new MapSqlParameterSource("tgId", tgId), new TagResultSetExtractor());
    }

    @Override
    public List<Tag> findAll() {
        var sql = "SELECT * FROM tag";
        return jdbc.query(sql, new TagResultSetExtractor());
    }
}
