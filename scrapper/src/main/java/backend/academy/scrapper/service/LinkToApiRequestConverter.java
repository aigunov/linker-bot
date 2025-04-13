package backend.academy.scrapper.service;

import backend.academy.scrapper.config.GitHubConfig;
import backend.academy.scrapper.config.StackOverflowConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@SuppressWarnings(value = {"REDOS"})
@SuppressFBWarnings(value = {"REDOS"})
@Component
@RequiredArgsConstructor
public class LinkToApiRequestConverter {

    private final GitHubConfig githubConfig;
    private final StackOverflowConfig stackOverflowConfig;

    private static final Pattern GITHUB_URL_PATTERN =
            Pattern.compile("^https://github\\.com/[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+/?$");

    private static final Pattern STACKOVERFLOW_URL_PATTERN =
            Pattern.compile("^https://(ru\\.)?stackoverflow\\.com/questions/\\d+/.+");

    public String convertGithubUrlToApi(String githubUrl) {
        if (!isGithubUrl(githubUrl)) {
            throw new IllegalArgumentException("Invalid GitHub URL format: " + githubUrl);
        }

        String repoPath = githubUrl.replace("https://github.com/", "").replaceAll("/$", "");
        return githubConfig.url() + "/" + repoPath;
    }

    public String convertStackOverflowUrlToApi(String stackOverflowUrl) {
        if (!isStackOverflowUrl(stackOverflowUrl)) {
            throw new IllegalArgumentException("Invalid StackOverflow URL format: " + stackOverflowUrl);
        }

        Matcher matcher = STACKOVERFLOW_URL_PATTERN.matcher(stackOverflowUrl);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Cannot extract question ID: " + stackOverflowUrl);
        }

        String questionId =
                stackOverflowUrl.replaceAll("^https://(ru\\.)?stackoverflow\\.com/questions/(\\d+)/.*", "$2");
        return stackOverflowConfig.url() + "/" + questionId + "?order=desc&sort=activity&site=ru.stackoverflow";
    }

    public boolean isGithubUrl(String url) {
        return GITHUB_URL_PATTERN.matcher(url).matches();
    }

    public boolean isStackOverflowUrl(String url) {
        return STACKOVERFLOW_URL_PATTERN.matcher(url).matches();
    }
}
