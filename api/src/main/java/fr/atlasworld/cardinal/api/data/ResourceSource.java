package fr.atlasworld.cardinal.api.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import fr.atlasworld.cardinal.api.exception.data.DataLoadingException;
import fr.atlasworld.cardinal.api.exception.data.DataSerializationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Holds the source of the data and prevents wasteful operations on memory when loading data elements.
 */
public interface ResourceSource {

    /**
     * Retrieve the source pack from which the data comes from.
     *
     * @return source datapack.
     */
    @NotNull Datapack sourceDatapack();

    /**
     * Opens an input stream to access the underlying data source.
     * The stream should be closed after use to release any system resources associated with it.
     *
     * @return a non-null {@link InputStream} for reading the data.
     * @throws DataLoadingException if an I/O error occurs while attempting to open the stream.
     */
    @NotNull InputStream openStream() throws DataLoadingException;

    /**
     * Open the current source as a JSON.
     * <p>
     * This method reads and consumes the underlying stream. And thus will reopen a stream everytime.
     * <p>
     * <b>Warning:</b> on big JSON files,
     * reading files chunks by chunks using {@link #openStream()} may be performant in memory usage.
     *
     * @return a {@link JsonElement} parsed from the data.
     *
     * @throws DataLoadingException       if an I/O error occurs while opening the stream.
     * @throws DataSerializationException if the data could not be parsed as JSON.
     */
    default @NotNull JsonElement asJson() throws DataLoadingException, DataSerializationException {
        try (InputStream stream = openStream();
             InputStreamReader reader = new InputStreamReader(stream)) {

            try {
                return JsonParser.parseReader(reader);
            } catch (Throwable ex) {
                throw new DataSerializationException("Source is corrupted or invalid json data: ", ex);
            }
        } catch (IOException ex) {
            throw new DataLoadingException("Failed to open source stream: ", ex);
        }
    }
}
