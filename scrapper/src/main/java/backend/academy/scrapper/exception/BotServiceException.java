package backend.academy.scrapper.exception;

public class BotServiceException extends RuntimeException {
    public BotServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
