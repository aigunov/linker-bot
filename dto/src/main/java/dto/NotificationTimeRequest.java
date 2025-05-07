package dto;

import lombok.Builder;
import java.time.LocalTime;

@Builder
public record NotificationTimeRequest(LocalTime time) {
}
