package backend.academy.scrapper.repository.tag;

import backend.academy.scrapper.data.model.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class TagResultSetExtractor implements ResultSetExtractor<List<Tag>> {
    @Override
    public @NotNull List<Tag> extractData(ResultSet rs) throws SQLException, DataAccessException {
        var tags = new LinkedList<Tag>();
        while (rs.next()) {
            tags.add(Tag.builder()
                .id(UUID.fromString(rs.getString("id")))
                .tag(rs.getString("tag"))
                .build()
            );
        }
        log.debug("Result set converted into List<Tag>: {}", tags);
        return tags;
    }
}
