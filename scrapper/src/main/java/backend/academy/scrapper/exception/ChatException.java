package backend.academy.scrapper.exception;

public class ChatException extends RuntimeException {
    public ChatException(String message) {
        super(message);
    }

    public ChatException(String message, Long chatId) {
        super(String.format(message, chatId));
    }
}
