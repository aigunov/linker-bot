package backend.academy.scrapper.repository.link;

import backend.academy.scrapper.data.model.Chat;
import backend.academy.scrapper.data.model.Filter;
import backend.academy.scrapper.data.model.Link;
import backend.academy.scrapper.data.model.Tag;
import jakarta.validation.constraints.NotNull;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Map<UUID, Link> linkMap = new HashMap<>();

        ResultSetMetaData metaData = rs.getMetaData();
        Set<String> columnNames = new HashSet<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columnNames.add(metaData.getColumnName(i));
        }

        while (rs.next()) {
            UUID linkId = UUID.fromString(rs.getString("id"));
            Link link = linkMap.computeIfAbsent(linkId, id -> {
                try {
                    return Link.builder()
                        .id(id)
                        .url(rs.getString("url"))
                        .lastUpdate(rs.getTimestamp("last_update").toLocalDateTime())
                        .chats(new HashSet<>())
                        .tags(new HashSet<>())
                        .filters(new HashSet<>())
                        .build();
                } catch (SQLException e) {
                    log.error("Error processing ResultSet row", e);
                    throw new DataAccessException("Error processing ResultSet row", e) {};
                }
            });

            if (columnNames.contains("chat_id")) {
                String chatIdStr = rs.getString("chat_id");
                if (chatIdStr != null) {
                    link.chats().add(Chat.builder()
                        .id(UUID.fromString(chatIdStr))
                        .tgId(rs.getLong("tg_id"))
                        .nickname(rs.getString("nickname"))
                        .build());
                }
            }

            if (columnNames.contains("tag_id")) {
                String tagIdStr = rs.getString("tag_id");
                if (tagIdStr != null) {
                    link.tags().add(Tag.builder()
                        .id(UUID.fromString(tagIdStr))
                        .tag(rs.getString("tag"))
                        .build());
                }
            }

            if (columnNames.contains("filter_id")) {
                String filterIdStr = rs.getString("filter_id");
                if (filterIdStr != null) {
                    link.filters().add(Filter.builder()
                        .id(UUID.fromString(filterIdStr))
                        .parameter(rs.getString("parameter"))
                        .value(rs.getString("value"))
                        .build());
                }
            }
        }

        var links = new ArrayList<>(linkMap.values());
        log.debug("Result set converted into List<Link>: {}", links);
        return links;
    }
}
