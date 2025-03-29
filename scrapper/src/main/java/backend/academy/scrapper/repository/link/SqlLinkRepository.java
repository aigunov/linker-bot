package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.exception.SqlRepositoryException;
import java.util.List;
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
@ConditionalOnProperty(prefix="app.db", name = "access-type", havingValue="sql")
public class SqlLinkRepository implements LinkRepository{
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void deleteById(final UUID id) {
        // Удаление связей из tag_to_link
        var deleteLinkTagSql = """
            DELETE
            FROM tag_to_link
            WHERE link_id = :linkId
            """;
        jdbc.update(deleteLinkTagSql, new MapSqlParameterSource("linkId", id.toString()));

        // Удаление связей из link_to_filter
        var deleteLinkFilterSql = """
            DELETE
            FROM link_to_chat
            WHERE link_id = :linkId
            """;
        jdbc.update(deleteLinkFilterSql, new MapSqlParameterSource("linkId", id.toString()));

        // Удаление связей из link_to_chat
        var deleteLinkChatSql = """
            DELETE
            FROM link_to_chat
            WHERE link_id = :linkId
            """;
        jdbc.update(deleteLinkChatSql, new MapSqlParameterSource("linkId", id.toString()));

        // Удаление link из таблицы link
        String sql = "DELETE FROM link WHERE id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id.toString()));

        // Удаление пустых tags
        String deleteEmptyTagsSql = """
            DELETE
            FROM tag
            WHERE id NOT IN (SELECT tag_id
                             FROM tag_to_link)
            """;
        jdbc.update(deleteEmptyTagsSql, new MapSqlParameterSource());

        // Удаление пустых filters
        String deleteEmptyFiltersSql = """
            DELETE
            FROM filter
            WHERE id NOT IN (SELECT filter_id
                             FROM link_to_filter)
            """;
        jdbc.update(deleteEmptyFiltersSql, new MapSqlParameterSource());
    }

    @Override
    public Optional<Link> findById(UUID id) {
        var sql = """
            SELECT l.*, c.id as chat_id, c.tg_id, c.nickname,
                t.id as tag_id, t.tag, f.id as filter_id, f.parameter, f.value
            FROM link AS l
            LEFT JOIN link_to_chat ltc ON l.id = ltc.link_id
            LEFT JOIN chat AS c ON ltc.chat_id = c.id
            LEFT JOIN tag_to_link AS ttl ON l.id = ttl.link_id
            LEFT JOIN tag AS t ON t.id = ttl.tag_id
            LEFT JOIN link_to_filter AS ltf ON ltf.link_id = l.id
            LEFT JOIN filter AS f ON ltf.filter_id = f.id
            WHERE l.id = : id
            """;

        var result = jdbc.query(sql, new MapSqlParameterSource("id", id), new LinkResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public Optional<Link> findByTgIdAndUrl(final Long tgId, final String url) {
        var sql = """
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
        var params = new MapSqlParameterSource()
            .addValue("tg_id", tgId)
            .addValue("url", url);
        var result = jdbc.query(sql, params, new LinkResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public List<Link> findAll() {
        var sql = """
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
        var sql = """
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
    public List<Link> findAllByTgId(final Long tgId) {
        var sql = """
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
    public List<Link> findLinksByTgIdAndTags(final Long chatId, final List<String> tags, final long size) {
        var sql = """
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
            LIMIT :limit
            """;
        var params = new MapSqlParameterSource()
            .addValue("tgId", chatId)
            .addValue("tags", tags)
            .addValue("size", size);
        return jdbc.query(sql, params, new LinkResultSetExtractor());
    }

    //todo: переработать для того чтобы метод работал еще как и update
//    @Transactional(propagation = Propagation.MANDATORY)
//    @Override
//    public Link save(final Link link) {
//        Optional<Link> existingLink = findByUrl(link.url());
//
//
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        //Сохранение в link
//        var sql = """
//            INSERT INTO link(url, last_update)
//            VALUES (:url, :lastUpdate)
//            """;
//
//        var params = new MapSqlParameterSource()
//            .addValue("url", link.url())
//            .addValue("lastUpdate", link.lastUpdate());
//
//        jdbc.update(sql, params, keyHolder);
//        link.id((UUID) keyHolder.getKeys().get("id"));
//
//
//        //Сохранение в link_to_chat
//        var linkChatSql = """
//            INSERT INTO link_to_chat(chat_id, link_id)
//            VALUES (:chatId, :linkId)
//            """;
//
//        if (link.chats().stream().findFirst().isEmpty()) {
//            throw new SqlRepositoryException("No chat found");
//        }
//
//        var linkChatParams = new MapSqlParameterSource()
//            .addValue("chatId", link.chats().stream().findFirst().get().id().toString())
//            .addValue("linkId", link.id().toString());
//
//        jdbc.update(linkChatSql, linkChatParams);
//
//        //Сохранение в tag_to_link
//        var linkTagSql = """
//            INSERT INTO tag_to_link(link_id, tag_id)
//            VALUES (:linkId, :tagId)
//            """;
//
//        link.tags().forEach(tag -> {
//            var linkTagParams = new MapSqlParameterSource()
//                .addValue("linkId", link.id().toString())
//                .addValue("tagId", tag.id().toString());
//            jdbc.update(linkTagSql, linkTagParams);
//        });
//
//        ////Сохранение в link_to_filter
//        var linkFilterSql = """
//            INSERT INTO link_to_filter(link_id, filter_id)
//            VALUES (:linkId, :filterId)
//            """;
//
//        link.filters().forEach(filter -> {
//            var linkFilterParams = new MapSqlParameterSource()
//                .addValue("linkId", link.id().toString())
//                .addValue("filterId", filter.id().toString());
//            jdbc.update(linkFilterSql, linkFilterParams);
//        });
//
//        return link;
//    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public Link save(final Link link) {
        Optional<Link> existingLink = findByUrl(link.url());
        if (existingLink.isPresent()) {
            // Обновляем lastUpdate и добавляем новые связи
            Link existing = existingLink.get();
            existing.lastUpdate(link.lastUpdate());
            // Добавляем новые связи в link_to_chat
            saveLinkToChat(link);
            // Добавляем новые связи в tag_to_link
            saveLinkToTag(link);
            // Добавляем новые связи в link_to_filter
            saveLinkToFilter(link);

            return existing;
        } else {
            // Сохранение новой записи
            KeyHolder keyHolder = new GeneratedKeyHolder();
            String insertLinkSql = """
                INSERT INTO link(url, last_update)
                VALUES (:url, :lastUpdate)
                RETURNING id
                """;
            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                .addValue("url", link.url())
                .addValue("lastUpdate", link.lastUpdate());
            jdbc.update(insertLinkSql, insertParams, keyHolder);
            link.id((UUID) keyHolder.getKeys().get("id"));
            // Сохранение связей в link_to_chat
            saveLinkToChat(link);
            // Сохранение связей в tag_to_link
            saveLinkToTag(link);
            // Сохранение связей в link_to_filter
            saveLinkToFilter(link);

            return link;
        }
    }

    private Optional<Link> findByUrl(String url) {
        String sql = "SELECT * FROM link WHERE url = :url";
        MapSqlParameterSource params = new MapSqlParameterSource("url", url);
        List<Link> results = jdbc.query(sql, params, new LinkResultSetExtractor());
        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    private void saveLinkToChat(Link link) {
        if (link.chats().stream().findFirst().isEmpty()) {
            throw new SqlRepositoryException("No chat found");
        }

        var linkChatSql = """
            INSERT INTO link_to_chat(chat_id, link_id)
            VALUES (:chatId, :linkId)
            """;

        var linkChatParams = new MapSqlParameterSource()
            .addValue("chatId", link.chats().stream().findFirst().get().id().toString())
            .addValue("linkId", link.id().toString());

        jdbc.update(linkChatSql, linkChatParams);
    }

    private void saveLinkToTag(Link link) {
        var linkTagSql = """
            INSERT INTO tag_to_link(link_id, tag_id)
            VALUES (:linkId, :tagId)
            """;

        link.tags().forEach(tag -> {
            var linkTagParams = new MapSqlParameterSource()
                .addValue("linkId", link.id().toString())
                .addValue("tagId", tag.id().toString());
            jdbc.update(linkTagSql, linkTagParams);
        });
    }

    private void saveLinkToFilter(Link link) {
        var linkFilterSql = """
            INSERT INTO link_to_filter(link_id, filter_id)
            VALUES (:linkId, :filterId)
            """;

        link.filters().forEach(filter -> {
            var linkFilterParams = new MapSqlParameterSource()
                .addValue("linkId", link.id().toString())
                .addValue("filterId", filter.id().toString());
            jdbc.update(linkFilterSql, linkFilterParams);
        });
    }
}
