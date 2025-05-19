package dto;

import java.time.LocalTime;
import lombok.Builder;

@Builder
public record NotificationTimeRequest(LocalTime time) {}
