package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.exception.SqlRepositoryException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.db", name = "access-type", havingValue = "sql")
public class SqlLinkRepository implements LinkRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Link save(final Link link) {
        Optional<Link> existingLink = findByUrl(link.url());
        if (existingLink.isPresent()) {
            saveLinkToChat(link);
            saveLinkToTag(link);
            saveLinkToFilter(link);
            return existingLink.orElseThrow(() -> new IllegalStateException("Existing link expected to be present"));
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        String insertLinkSql =
                """
        INSERT INTO link(url, last_update)
        VALUES (:url, :lastUpdate)
        RETURNING id
        """;

        MapSqlParameterSource insertParams =
                new MapSqlParameterSource().addValue("url", link.url()).addValue("lastUpdate", link.lastUpdate());

        jdbc.update(insertLinkSql, insertParams, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            Object idValue = keys.get("id");
            if (idValue instanceof UUID uuid) {
                link.id(uuid);
            } else {
                throw new SqlRepositoryException("Generated ID 'id' is not a UUID or is null for link");
            }
        } else {
            throw new SqlRepositoryException("Failed to retrieve generated ID for link");
        }

        saveLinkToChat(link);
        saveLinkToTag(link);
        saveLinkToFilter(link);

        return link;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void deleteById(final UUID id) {
        var deleteLinkTagSql =
                """
                DELETE
                FROM tag_to_link
                WHERE link_id = :linkId
                """;
        jdbc.update(deleteLinkTagSql, new MapSqlParameterSource("linkId", id));

        var deleteLinkFilterSql =
                """
                DELETE
                FROM link_to_chat
                WHERE link_id = :linkId
                """;
        jdbc.update(deleteLinkFilterSql, new MapSqlParameterSource("linkId", id));

        var deleteLinkChatSql =
                """
                DELETE
                FROM link_to_chat
                WHERE link_id = :linkId
                """;
        jdbc.update(deleteLinkChatSql, new MapSqlParameterSource("linkId", id));

        String sql = "DELETE FROM link WHERE id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Link> findById(UUID id) {
        var sql =
                """
                SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                    t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id
                LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
                LEFT JOIN tag AS t ON t.id = ttl.tag_id
                LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
                LEFT JOIN filter AS f ON ltf.filter_id = f.id
                WHERE l.id = :id
                """;

        var result = jdbc.query(sql, new MapSqlParameterSource("id", id), new LinkResultSetExtractor());
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().findFirst();
    }

    @Override
    public Optional<Link> findByTgIdAndUrl(final Long tgId, final String url) {
        var sql =
                """
                SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                    t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id
                LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
                LEFT JOIN tag AS t ON t.id = ttl.tag_id
                LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
                LEFT JOIN filter AS f ON ltf.filter_id = f.id
                WHERE c.tg_id = :tgId AND l.url = :url
                """;
        var params = new MapSqlParameterSource().addValue("tgId", tgId).addValue("url", url);
        var result = jdbc.query(sql, params, new LinkResultSetExtractor());
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().findFirst();
    }

    @Override
    public List<Link> findAll() {
        var sql =
                """
                SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                    t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id
                LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
                LEFT JOIN tag AS t ON t.id = ttl.tag_id
                LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
                LEFT JOIN filter AS f ON ltf.filter_id = f.id
                """;
        return jdbc.query(sql, new LinkResultSetExtractor());
    }

    @Override
    public List<Link> findAll(final Pageable pageable) {
        var sql =
                """
                SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                    t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id
                LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
                LEFT JOIN tag AS t ON t.id = ttl.tag_id
                LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
                LEFT JOIN filter AS f ON ltf.filter_id = f.id
                LIMIT :limit
                OFFSET :offset
                """;
        var params = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());
        return jdbc.query(sql, params, new LinkResultSetExtractor());
    }

    @Override
    public List<Link> findAllWithChats(final Pageable pageable) {
        var sql =
                """
                SELECT l.*, c.id AS chat_id, c.tg_id, c.nickname
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id

                LIMIT :limit OFFSET :offset
                """;

        var params = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());
        return jdbc.query(sql, params, new LinkResultSetExtractor());
    }

    @Override
    public List<Link> findAllByTgId(final Long tgId) {
        var sql =
                """
                SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                    t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id
                LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
                LEFT JOIN tag AS t ON t.id = ttl.tag_id
                LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
                LEFT JOIN filter AS f ON ltf.filter_id = f.id
                WHERE c.tg_id = :tgId
                """;
        return jdbc.query(sql, new MapSqlParameterSource("tgId", tgId), new LinkResultSetExtractor());
    }

    @Override
    public List<Link> findLinksByTgIdAndTags(final Long tgId, final List<String> tags) {
        var sql =
                """
                SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                    t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
                FROM link AS l
                LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
                LEFT JOIN chat AS c ON ltc.chat_id = c.id
                LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
                LEFT JOIN tag AS t ON t.id = ttl.tag_id
                LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
                LEFT JOIN filter AS f ON ltf.filter_id = f.id
                WHERE c.tg_id = :tgId AND t.tag IN (:tags)
                """;
        var params = new MapSqlParameterSource().addValue("tgId", tgId).addValue("tags", tags);
        return jdbc.query(sql, params, new LinkResultSetExtractor());
    }

    @Override
    public void deleteAll() {
        var sql = """
            DELETE FROM link
            """;
        jdbc.update(sql, new MapSqlParameterSource());
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        String sql = "SELECT * FROM link WHERE url = :url";
        MapSqlParameterSource params = new MapSqlParameterSource("url", url);
        List<Link> results = jdbc.query(sql, params, new LinkResultSetExtractor());
        if (results == null) {
            return Optional.empty();
        }
        return results.stream().findFirst();
    }

    private void saveLinkToChat(Link link) {
        Optional<Chat> firstChatOptional = link.chats().stream().findFirst();
        Chat chat = firstChatOptional.orElseThrow(() -> new SqlRepositoryException("No chat found"));

        var linkChatSql =
                """
                INSERT INTO link_to_chat(chat_id, link_id)
                VALUES (:chatId, :linkId)
                """;

        var linkChatParams =
                new MapSqlParameterSource().addValue("chatId", chat.id()).addValue("linkId", link.id());

        jdbc.update(linkChatSql, linkChatParams);
    }

    private void saveLinkToTag(Link link) {
        var linkTagSql =
                """
                INSERT INTO tag_to_link(link_id, tag_id)
                VALUES (:linkId, :tagId)
                """;

        link.tags().forEach(tag -> {
            var linkTagParams =
                    new MapSqlParameterSource().addValue("linkId", link.id()).addValue("tagId", tag.id());
            jdbc.update(linkTagSql, linkTagParams);
        });
    }

    private void saveLinkToFilter(Link link) {
        var linkFilterSql =
                """
                INSERT INTO link_to_filter(link_id, filter_id)
                VALUES (:linkId, :filterId)
                """;

        link.filters().forEach(filter -> {
            var linkFilterParams =
                    new MapSqlParameterSource().addValue("linkId", link.id()).addValue("filterId", filter.id());
            jdbc.update(linkFilterSql, linkFilterParams);
        });
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public <S extends Link> List<S> saveAll(Iterable<S> links) {
        var list = new ArrayList<S>();
        for (Link link : links) {
            list.add((S) save(link));
        }
        return list;
    }
}
