package backend.academy.scrapper.exception;

public class ScrapperServicesApiException extends RuntimeException {
    public ScrapperServicesApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrapperServicesApiException(String message) {
        super(message);
    }

    public ScrapperServicesApiException() {}
}
