package backend.academy.scrapper.exception;

public class GitHubApiException extends ScrapperServicesApiException {

    public GitHubApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHubApiException(String message) {
        super(message);
    }
}
