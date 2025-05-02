package backend.academy.scrapper.exception;

public class LinkException extends RuntimeException {
    public LinkException(String message) {
        super(message);
    }

    public LinkException(String message, String uri, Long chatId) {
        super(String.format(message, uri, chatId));
    }
}
