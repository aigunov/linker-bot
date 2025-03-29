package backend.academy.scrapper.repository.chat;


import backend.academy.scrapper.data.model.Chat;
import io.micrometer.tracing.otel.propagation.PropagationType;
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
public class SqlChatRepository implements ChatRepository {

    private final NamedParameterJdbcTemplate jdbc;

    //todo: Returning id?
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Chat save(Chat chat) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        var sql = """
            INSERT INTO chat (tg_id, nickname)
            VALUES (:tgId, :nickname)
            """;
        var params = new MapSqlParameterSource()
            .addValue("tgId", chat.tgId())
            .addValue("nickname", chat.nickname());

        jdbc.update(sql, params, keyHolder);
        chat.id((UUID) keyHolder.getKeys().get("id"));

        return chat;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @Override
    public void deleteById(UUID id) {
        var deleteLinkChatSql = """
            DELETE
            FROM link_to_chat
            WHERE chat_id = :chatId
            """;
        jdbc.update(deleteLinkChatSql, new MapSqlParameterSource("chatId", id.toString()));

        var sql = """
            DELETE
            FROM chat
            WHERE id = :id
            """;
        jdbc.update(sql, new MapSqlParameterSource("id", id.toString()));
    }

    @Transactional
    @Override
    public void deleteByTgId(Long tgId) {
        String deleteLinkChatSql = """
                DELETE
                FROM link_to_chat
                WHERE chat_id = (SELECT id FROM chat WHERE tg_id = :tgId)
                """;
        jdbc.update(deleteLinkChatSql, new MapSqlParameterSource("tgId", tgId));

        String sql = """
            DELETE
            FROM chat
            WHERE tg_id = :tgId""";
        jdbc.update(sql, new MapSqlParameterSource("tgId", tgId));
    }

    @Override
    public Optional<Chat> findById(UUID id) {
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
