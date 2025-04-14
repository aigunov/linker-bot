package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
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
        chat.id((UUID) keyHolder.getKeys().get("id"));

        return chat;
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

    // todo: удаление связанных
    @Override
    public void deleteAll() {
        jdbc.update("DELETE FROM Chat", new MapSqlParameterSource());
    }

    // todo: переработать
    @Override
    public <S extends Chat> List<S> saveAll(Iterable<S> chats) {
        for (Chat chat : chats) {
            save(chat);
        }
        return List.of();
    }

    @Override
    public Optional<Chat> findById(final UUID id) {
        var sql = """
            SELECT *
            FROM chat
            WHERE id = :id
            """;
        var result = jdbc.query(sql, new MapSqlParameterSource("id", id), new ChatResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public Optional<Chat> findByTgId(final Long tgId) {
        var sql = """
            SELECT *
            FROM chat
            WHERE tg_id = :tgId
            """;
        var result = jdbc.query(sql, new MapSqlParameterSource("tgId", tgId), new ChatResultSetExtractor());
        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    @Override
    public List<Chat> findAll() {
        String sql = "SELECT * FROM chat";
        return jdbc.query(sql, new ChatResultSetExtractor());
    }
}
