package backend.academy.scrapper.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StackOverflowResponse {
    private List<StackOverflowItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class StackOverflowItem {
        @JsonProperty("last_activity_date")
        private Long lastActivityDate;

    }
}

