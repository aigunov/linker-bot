package backend.academy.scrapper.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowResponse {
    private List<StackOverflowItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StackOverflowItem {
        @JsonProperty("last_activity_date")
        private long lastActivityDate;

        private String title;
        private List<Answer> answers;
        private List<Comment> comments;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Answer {
            @JsonProperty("creation_date")
            private long creationDate;
            @JsonProperty("owner")
            private Owner owner;
            private String body;

            @Getter
            @Setter
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Owner{
                @JsonProperty("display_name")
                private String displayName;
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Comment{
            @JsonProperty("creation_date")
            private long creationDate;
            @JsonProperty("owner")
            private Owner owner;
            private String body;

            @Getter
            @Setter
            @NoArgsConstructor
            @AllArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Owner {
                @JsonProperty("display_name")
                private String displayName;
            }
        }
    }
}
