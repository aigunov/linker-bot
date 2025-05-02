package backend.academy.scrapper.repository.filter;

import backend.academy.scrapper.data.model.Filter;
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
public class FilterResultSetExtractor implements ResultSetExtractor<List<Filter>> {
    @Override
    public @NotNull List<Filter> extractData(ResultSet rs) throws SQLException, DataAccessException {
        var filters = new LinkedList<Filter>();
        while (rs.next()) {
            filters.add(Filter.builder()
                    .id(UUID.fromString(rs.getString("id")))
                    .parameter(rs.getString("parameter"))
                    .value(rs.getString("value"))
                    .build());
        }
        log.debug("Result set converted into List<Filter> : {}", filters);
        return filters;
    }
}
