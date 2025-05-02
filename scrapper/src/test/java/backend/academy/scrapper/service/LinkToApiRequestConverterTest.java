package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LinkToApiRequestConverterTest {

    private LinkToApiRequestConverter converter;

    @BeforeEach
    void setUp() {
        GitHubConfig githubConfig = new GitHubConfig("fake-token", "https://api.github.com/repos");
        StackOverflowConfig stackOverflowConfig =
                new StackOverflowConfig("fake-key", "fake-access", "https://api.stackexchange.com/2.3/questions");

        converter = new LinkToApiRequestConverter(githubConfig, stackOverflowConfig);
    }

    @Test
    void convertGithubUrlToApi_shouldConvertValidGithubUrl() {
        String githubUrl = "https://github.com/aigunov/backend-academy";
        String expected = "https://api.github.com/repos/aigunov/backend-academy";

        String result = converter.convertGithubUrlToApi(githubUrl);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void convertStackOverflowUrlToApi_shouldConvertValidStackOverflowUrl() {
        String url = "https://ru.stackoverflow.com/questions/1607351/question-title";
        String expected =
                "https://api.stackexchange.com/2.3/questions/1607351?order=desc&sort=activity&site=ru.stackoverflow";

        String result = converter.convertStackOverflowUrlToApi(url);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void isGithubUrl_shouldReturnTrueForGithubUrl() {
        assertThat(converter.isGithubUrl("https://github.com/aigunov/test")).isTrue();
    }

    @Test
    void isGithubUrl_shouldReturnFalseForNonGithubUrl() {
        assertThat(converter.isGithubUrl("https://stackoverflow.com/questions/123"))
                .isFalse();
    }

    @Test
    void isStackOverflowUrl_shouldReturnFalseForNonStackOverflowUrl() {
        assertThat(converter.isStackOverflowUrl("https://github.com/aigunov/test"))
                .isFalse();
    }

    @Test
    void convertGithubUrlToApi_shouldThrowOnInvalidUrl() {
        String invalidUrl = "https://notgithub.com/user/repo";
        assertThatThrownBy(() -> converter.convertGithubUrlToApi(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid GitHub URL format");
    }

    @Test
    void convertStackOverflowUrlToApi_shouldThrowOnInvalidUrl() {
        String invalidUrl = "https://not.stackoverflow.com/question/123";
        assertThatThrownBy(() -> converter.convertStackOverflowUrlToApi(invalidUrl))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid StackOverflow URL format");
    }
}
