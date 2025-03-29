package backend.academy.scrapper.exception;

public class SqlRepositoryException extends RuntimeException {
    public SqlRepositoryException(String message) {
        super(message);
    }
}
