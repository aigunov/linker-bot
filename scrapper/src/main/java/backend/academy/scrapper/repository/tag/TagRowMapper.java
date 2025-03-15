package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TagRowMapper implements RowMapper<Tag> {
    @Override
    public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
