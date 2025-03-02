package backend.academy.scrapper.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.github", ignoreUnknownFields = false)
public record GitHubConfig (@NotBlank String token,
                            @NotBlank String url){
}
