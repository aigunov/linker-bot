package backend.academy.scrapper.service;


import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkToApiRequestConverter {
    private final GitHubConfig githubConfig;
    private final StackOverflowConfig stackOverflowConfig;

    /**
     * Converts a GitHub repository URL to a GitHub API request URL.
     *
     * @param githubUrl the GitHub repository URL (e.g., <a href="https://github.com/user/repository">...</a>)
     * @return the GitHub API URL (e.g., <a href="https://api.github.com/repos/user/repository">...</a>)
     */
    public String convertGithubUrlToApi(String githubUrl) {
        if (!githubUrl.matches("""
            https://github.com/[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+/?""") || !isGithubUrl(githubUrl)) {
            throw new IllegalArgumentException(STR."Invalid GitHub URL format: \{githubUrl}");
        }

        String repoPath = githubUrl.replace("https://github.com/", "").replaceAll("/$", "");
        return STR."\{githubConfig.url()}/\{repoPath}";
    }

    /**
     * Converts a StackO
     * verflow question URL to a StackOverflow API request URL.
     *
     * @param stackOverflowUrl the StackOverflow question URL (e.g., <a href="https://stackoverflow.com/questions/12345678/question-title">...</a>)
     * @return the StackOverflow API URL (e.g., <a href="https://api.stackexchange.com/2.3/questions/12345678?order=desc&sort=activity&site=stackoverflow">...</a>)
     */
    public String convertStackOverflowUrlToApi(String stackOverflowUrl) {
        if (!stackOverflowUrl.matches("""
            https://(ru\\.)?stackoverflow.com/questions/\\d+/.*""") || !isStackOverflowUrl(stackOverflowUrl)) {
            throw new IllegalArgumentException("Invalid StackOverflow URL format: " + stackOverflowUrl);
        }

        String questionId = stackOverflowUrl.replaceAll("""
            https://(ru\\.)?stackoverflow.com/questions/(\\d+)/.*""", "$2");
        return STR."\{stackOverflowConfig.url()}/\{questionId}?order=desc&sort=activity&site=ru.stackoverflow";
    }
    /**
     * Determines if the provided URL is a GitHub repository URL.
     *
     * @param url the URL to check
     * @return true if it is a GitHub repository URL, false otherwise
     */
    public boolean isGithubUrl(String url) {
        return url.startsWith("https://github.com/");
    }

    /**
     * Determines if the provided URL is a StackOverflow question URL.
     *
     * @param url the URL to check
     * @return true if it is a StackOverflow question URL, false otherwise
     */
    public boolean isStackOverflowUrl(String url) {
        return url.contains("stackoverflow.com");
    }
}
