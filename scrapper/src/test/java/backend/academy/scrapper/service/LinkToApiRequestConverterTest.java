package backend.academy.scrapper.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LinkToApiRequestConverterTest {

    private LinkToApiRequestConverter converter;

    @BeforeEach
    void setUp() {
        converter = new LinkToApiRequestConverter();
    }

    @Test
    void convertGithubUrlToApi_shouldConvertValidGithubUrl() {
        String githubUrl = "https://github.com/aigunov/backend-academy";
        String expectedApiUrl = "https://api.github.com/repos/aigunov/backend-academy";

        String actualApiUrl = converter.convertGithubUrlToApi(githubUrl);

        assertThat(actualApiUrl).isEqualTo(expectedApiUrl);
    }

    @Test
    void convertStackOverflowUrlToApi_shouldConvertValidStackOverflowUrl() {
        String stackOverflowUrl = "https://ru.stackoverflow.com/questions/1607351/%d0%9f%d0%be%d1%81%d0%bb%d0%b5%d0%b4%d0%be%d0%b2%d0%b0%d1%82%d0%b5%d0%bb%d1%8c%d0%bd%d1%8b%d0%b9-%d0%b2%d1%8b%d0%b7%d0%be%d0%b2-%d1%84%d1%83%d0%bd%d0%ba%d1%86%d0%b8%d0%b9-%d0%b2-java";
        String expectedApiUrl = "https://api.stackexchange.com/2.3/questions/1607351?order=desc&sort=activity&site=ru.stackoverflow";

        String actualApiUrl = converter.convertStackOverflowUrlToApi(stackOverflowUrl);

        assertThat(actualApiUrl).isEqualTo(expectedApiUrl);
    }

    @Test
    void isGithubUrl_shouldReturnTrueForGithubUrl() {
        String githubUrl = "https://github.com/aigunov/backend-academy";
        assertThat(converter.isGithubUrl(githubUrl)).isTrue();
    }

    @Test
    void isGithubUrl_shouldReturnFalseForNonGithubUrl() {
        String nonGithubUrl = "https://ru.stackoverflow.com/questions/1607351";
        assertThat(converter.isGithubUrl(nonGithubUrl)).isFalse();
    }

    @Test
    void isStackOverflowUrl_shouldReturnTrueForStackOverflowUrl() {
        String stackOverflowUrl = "https://ru.stackoverflow.com/questions/1607351";
        assertThat(converter.isStackOverflowUrl(stackOverflowUrl)).isTrue();
    }

    @Test
    void isStackOverflowUrl_shouldReturnFalseForNonStackOverflowUrl() {
        String nonStackOverflowUrl = "https://github.com/aigunov/backend-academy";
        assertThat(converter.isStackOverflowUrl(nonStackOverflowUrl)).isFalse();
    }
}
