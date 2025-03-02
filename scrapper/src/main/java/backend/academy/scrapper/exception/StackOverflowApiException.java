package backend.academy.scrapper.exception;

public class StackOverflowApiException extends ScrapperServicesApiException {

    public StackOverflowApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public StackOverflowApiException(String message) {
        super(message);
    }
}
