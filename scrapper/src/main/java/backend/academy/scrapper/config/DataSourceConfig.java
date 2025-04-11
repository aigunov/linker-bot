package backend.academy.scrapper.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.datasource")
public record DataSourceConfig(String url, String username, String password) {
}
