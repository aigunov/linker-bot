package dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder(toBuilder = true)
public record NotificationMessage(@NotNull String uri, @NotNull LocalDateTime updateDate) {}
