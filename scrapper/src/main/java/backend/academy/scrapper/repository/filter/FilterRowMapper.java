package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FilterRowMapper implements RowMapper<Filter> {
    @Override
    public Filter mapRow(ResultSet rs, int rowNum) throws SQLException {
        return null;
    }
}
