package backend.academy.scrapper.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Chat {
    @NotNull
    private UUID id;
    @NotNull
    private Long chatId;
    @NotNull
    private String username;
    @CreatedDate
    private LocalDateTime creationDate;
}
