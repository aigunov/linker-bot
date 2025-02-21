package backend.academy.scrapper.service;

import org.springframework.stereotype.Component;
import static backend.academy.scrapper.model.LinkingServices.GITHUB;
import static backend.academy.scrapper.model.LinkingServices.STACKOVERFLOW;

@Component
public class LinkToApiRequestConverter {

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
        return STR."\{GITHUB.APi_URL()}/\{repoPath}";
    }

    /**
     * Converts a StackOverflow question URL to a StackOverflow API request URL.
     *
     * @param stackOverflowUrl the StackOverflow question URL (e.g., <a href="https://stackoverflow.com/questions/12345678/question-title">...</a>)
     * @return the StackOverflow API URL (e.g., <a href="https://api.stackexchange.com/2.3/questions/12345678?order=desc&sort=activity&site=stackoverflow">...</a>)
     */
    public String convertStackOverflowUrlToApi(String stackOverflowUrl) {
        if (!stackOverflowUrl.matches("""
            https://stackoverflow.com/questions/\\d+/.*""") || !isStackOverflowUrl(stackOverflowUrl)) {
            throw new IllegalArgumentException("Invalid StackOverflow URL format: " + stackOverflowUrl);
        }

        String questionId = stackOverflowUrl.replaceAll("""
            https://stackoverflow.com/questions/(\\d+)/.*""", "$1");
        return STR."\{STACKOVERFLOW.APi_URL()}/\{questionId}?order=desc&sort=activity&site=stackoverflow";
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
        return url.startsWith("https://stackoverflow.com/questions/");
    }
}
