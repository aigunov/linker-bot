package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
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
public class LinkResultSetExtractor implements ResultSetExtractor<List<Link>> {
    @Override
    public @NotNull List<Link> extractData(ResultSet rs) throws SQLException, DataAccessException {
        var links = new LinkedList<Link>();
        while (rs.next()) {
            var link = Link.builder()
                .id(UUID.fromString(rs.getString("id")))
                .url(rs.getString("url"))
                .lastUpdate(rs.getTimestamp("last_update").toLocalDateTime())
                .build();

            link.chats().add(Chat.builder()
                .id(UUID.fromString(rs.getString("chat_id")))
                .tgId(rs.getLong("tg_id"))
                .nickname(rs.getString("nickname"))
                .build());

            link.tags().add(Tag.builder()
                .id(UUID.fromString(rs.getString("tag_id")))
                .tag(rs.getString("tag"))
                .build());

            link.filters().add(Filter.builder()
                .id(UUID.fromString(rs.getString("filter_id")))
                .parameter(rs.getString("parameter"))
                .value(rs.getString("value"))
                .build());
        }
        log.debug("Result set converted into List<Link>: {}", links);
        return links;
    }
}
