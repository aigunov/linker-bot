package dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record NotificationMessage (
    @NotNull String uri,
    @NotNull LocalDateTime updateDate
    ){}
