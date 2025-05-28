package backend.academy.scrapper;

import backend.academy.scrapper.config.DataSourceConfig;
import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRetry
@EnableScheduling
@EnableCaching
@SpringBootApplication
@EnableConfigurationProperties({GitHubConfig.class, StackOverflowConfig.class, DataSourceConfig.class})
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
