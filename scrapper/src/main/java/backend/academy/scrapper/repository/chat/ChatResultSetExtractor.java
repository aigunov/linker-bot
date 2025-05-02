package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import jakarta.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatResultSetExtractor implements ResultSetExtractor<List<Chat>> {
    @Override
    public @NotNull List<Chat> extractData(ResultSet rs) throws SQLException, DataAccessException {
        var chats = new LinkedList<Chat>();
        while (rs.next()) {
            chats.add(Chat.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .tgId(rs.getLong("tg_id"))
                    .nickname(rs.getString("nickname"))
                    .build());
        }
        log.debug("Result set converted into List<Chat>: {}", chats);
        return chats;
    }
}
