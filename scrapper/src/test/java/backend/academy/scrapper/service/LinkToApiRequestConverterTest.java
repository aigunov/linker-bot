package backend.academy.scrapper.service;

import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@EnableConfigurationProperties({GitHubConfig.class, StackOverflowConfig.class})
class LinkToApiRequestConverterTest {

    @Autowired
    private LinkToApiRequestConverter converter;

    @Autowired
    private GitHubConfig gitHubConfig;

    @Autowired
    private StackOverflowConfig stackOverflowConfig;

    @Test
    void convertGithubUrlToApi_shouldConvertValidGithubUrl() {
        // arrange
        String githubUrl = "https://github.com/aigunov/backend-academy";
        String expectedApiUrl = gitHubConfig.url() + "/aigunov/backend-academy";

        // act
        String actualApiUrl = converter.convertGithubUrlToApi(githubUrl);

        // assert
        assertThat(actualApiUrl).isEqualTo(expectedApiUrl);
    }

    @Test
    void convertStackOverflowUrlToApi_shouldConvertValidStackOverflowUrl() {
        // arrange
        String stackOverflowUrl = "https://ru.stackoverflow.com/questions/1607351/%d0%9f%d0%be%d1%81%d0%bb%d0%b5"
            + "%d0%b4%d0%be%d0%b2%d0%b0%d1%82%d0%b5%d0%bb%d1%8c%d0%bd%d1%8b%d0%b9-%d0%b2%d1%8b%d0%b7%d0%be%d0%b2-%d1%"
            + "84%d1%83%d0%bd%d0%ba%d1%86%d0%b8%d0%b9-%d0%b2-java";
        String expectedApiUrl = stackOverflowConfig.url() + "/1607351?order=desc&sort=activity&site=ru.stackoverflow";

        // act
        String actualApiUrl = converter.convertStackOverflowUrlToApi(stackOverflowUrl);

        // assert
        assertThat(actualApiUrl).isEqualTo(expectedApiUrl);
    }

    @Test
    void isGithubUrl_shouldReturnTrueForGithubUrl() {
        // arrange
        String githubUrl = "https://github.com/aigunov/backend-academy";

        // act & assert
        assertThat(converter.isGithubUrl(githubUrl)).isTrue();
    }

    @Test
    void isGithubUrl_shouldReturnFalseForNonGithubUrl() {
        // arrange
        String nonGithubUrl = "https://ru.stackoverflow.com/questions/1607351";

        // act & assert
        assertThat(converter.isGithubUrl(nonGithubUrl)).isFalse();
    }

    @Test
    void isStackOverflowUrl_shouldReturnTrueForStackOverflowUrl() {
        // arrange
        String stackOverflowUrl = "https://ru.stackoverflow.com/questions/1607351";

        // act & assert
        assertThat(converter.isStackOverflowUrl(stackOverflowUrl)).isTrue();
    }

    @Test
    void isStackOverflowUrl_shouldReturnFalseForNonStackOverflowUrl() {
        // arrange
        String nonStackOverflowUrl = "https://github.com/aigunov/backend-academy";

        // act & assert
        assertThat(converter.isStackOverflowUrl(nonStackOverflowUrl)).isFalse();
    }
}
