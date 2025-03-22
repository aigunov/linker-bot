package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.stackoverflow", ignoreUnknownFields = false)
public record StackOverflowConfig(@NotBlank String key, @NotBlank String access_token, @NotBlank String url) {}
