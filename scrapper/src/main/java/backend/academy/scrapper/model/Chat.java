package backend.academy.scrapper.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

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
