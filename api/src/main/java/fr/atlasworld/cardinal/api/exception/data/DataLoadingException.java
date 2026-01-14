package fr.atlasworld.cardinal.api.exception.data;

/**
 * Thrown when the data element or entry could not be loaded due to an IOException.
 */
public class DataLoadingException extends DataException {
    public DataLoadingException(Throwable cause) {
        super(cause);
    }

    public DataLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
