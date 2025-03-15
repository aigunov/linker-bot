package backend.academy.scrapper.repository.chat;

import backend.academy.scrapper.data.model.Chat;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatRowMapper implements RowMapper<Chat>{
    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
