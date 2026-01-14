package fr.atlasworld.cardinal.api.exception.data;

/**
 * Generic data exception.
 */
public class DataException extends Exception{
    public DataException() {
    }

    public DataException(String message) {
        super(message);
    }

    public DataException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataException(Throwable cause) {
        super(cause);
    }
}
