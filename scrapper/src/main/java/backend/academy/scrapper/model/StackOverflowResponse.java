package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.Data;

@Data
public class StackOverflowResponse {
    private List<StackOverflowItem> items;

    @Data
    public class StackOverflowItem {
        @JsonProperty("last_activity_date")
        private Long lastActivityDate;

    }
}

