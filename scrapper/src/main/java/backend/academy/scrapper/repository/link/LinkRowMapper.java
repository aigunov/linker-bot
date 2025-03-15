package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Link;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LinkRowMapper implements RowMapper<Link> {
    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
