package backend.academy.bot.exception;

public class FailedIncomingUpdatesHandleException extends RuntimeException {
    public FailedIncomingUpdatesHandleException(String message, Throwable cause) {
        super(message, cause);
    }
}
