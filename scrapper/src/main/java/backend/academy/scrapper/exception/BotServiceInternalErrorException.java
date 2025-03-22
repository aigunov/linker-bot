package backend.academy.scrapper.exception;

public class BotServiceInternalErrorException extends RuntimeException {
    public BotServiceInternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
