package backend.academy.scrapper.data.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public record UpdateInfo(String title,
                         String username,
                         LocalDateTime date,
                         String type,
                         String preview) {

    public String getFormattedMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = date.format(formatter);

        return String.format(
            "Название: %s\nПользователь: %s\nВремя: %s\nТип: %s\nПревью: %s",
            title,
            username,
            formattedDate,
            type,
            preview
        );
    }
}
