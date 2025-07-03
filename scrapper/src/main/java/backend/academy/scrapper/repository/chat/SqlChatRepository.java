package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.exception.SqlRepositoryException;
import java.time.LocalTime;
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
public class SqlChatRepository implements ChatRepository {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Chat save(Chat chat) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        var sql =
                """
            INSERT INTO chat (tg_id, nickname)
            VALUES (:tgId, :nickname)
            """;
        var params = new MapSqlParameterSource().addValue("tgId", chat.tgId()).addValue("nickname", chat.nickname());

        jdbc.update(sql, params, keyHolder);

        var keys = keyHolder.getKeys();
        if (keys != null && keys.containsKey("id")) {
            Object idValue = keys.get("id");
            if (idValue instanceof UUID uuid) {
                chat.id(uuid);
            } else {
                throw new SqlRepositoryException("Generated ID 'id' is not a UUID or is null for chat");
            }
        } else {
            throw new SqlRepositoryException("Failed to retrieve generated ID for chat");
        }

        return chat;
    }

    @Override
    public void setDigestTime(Long chatId, LocalTime time) {
        var sql =
                """
                    UPDATE chat
                    SET digest_time = :time
                    WHERE tg_id = :tgId
                """;

        var params = new MapSqlParameterSource().addValue("tgId", chatId).addValue("time", time);

        jdbc.update(sql, params);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void deleteById(final UUID id) {
        var sql = """
            DELETE
            FROM chat
            WHERE id = :id
            """;
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }

    @Transactional
    @Override
    public void deleteByTgId(final Long tgId) {

        String sql = """
            DELETE
            FROM chat
            WHERE tg_id = :tgId""";
        jdbc.update(sql, new MapSqlParameterSource("tgId", tgId));
    }

    @Override
    public void deleteAll() {
        jdbc.update("DELETE FROM Chat", new MapSqlParameterSource());
    }

    @Transactional
    @Override
    public <S extends Chat> List<S> saveAll(Iterable<S> chats) {
        var list = new ArrayList<S>();
        for (Chat chat : chats) {
            list.add((S) save(chat));
        }
        return list;
    }

    @Override
    public Optional<Chat> findById(final UUID id) {
        var sql = """
            SELECT *
            FROM chat
            WHERE id = :id
            """;
        List<Chat> result = jdbc.query(sql, new MapSqlParameterSource("id", id), new ChatResultSetExtractor());
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().findFirst();
    }

    @Override
    public Optional<Chat> findByTgId(final Long tgId) {
        var sql = """
            SELECT *
            FROM chat
            WHERE tg_id = :tgId
            """;
        List<Chat> result = jdbc.query(sql, new MapSqlParameterSource("tgId", tgId), new ChatResultSetExtractor());
        if (result == null) {
            return Optional.empty();
        }
        return result.stream().findFirst();
    }

    @Override
    public List<Chat> findAll() {
        String sql = "SELECT * FROM chat";
        return jdbc.query(sql, new ChatResultSetExtractor());
    }
}
