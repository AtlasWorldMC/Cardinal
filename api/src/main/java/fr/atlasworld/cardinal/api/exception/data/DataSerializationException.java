package fr.atlasworld.cardinal.api.exception.data;

/**
 * Thrown when a data resource fails to be serialized or deserialized.
 */
public class DataSerializationException extends DataException {
    public DataSerializationException(String message) {
        super(message);
    }

    public DataSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSerializationException(Throwable cause) {
        super(cause);
    }
}
