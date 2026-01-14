package fr.atlasworld.cardinal.api.data;

import fr.atlasworld.cardinal.api.exception.data.DataException;
import fr.atlasworld.cardinal.api.registry.Registry;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;

/**
 * Represents a data type, this is used to load and generate different types of data.
 *
 * @param <T> type of data contained in the data type.s
 */
public interface DataType<T> {

    /**
     * Retrieve whether the data type works with an index file contained in the namespace directory.
     * <br>
     * <b>Example:</b> {@code worlds.json} for world indexing.
     *
     * @return {@code true} if the data type works with an index file, {@code false} otherwise.
     */
    boolean indexed();

    /**
     * Retrieve the index file name.
     *
     * @return index file name, or {@code null} if the data type doesn't work with an {@link #indexed() index file}.
     */
    @UnknownNullability String indexFile();

    /**
     * Represents the directory in which the contents that this data type holds are located.
     * The type is unique, if two data types with the same type try to be registered, one will fail the loading.
     *
     * @return type of the data type.
     */
    @NotNull String type();

    /**
     * Retrieve the registry this data type uses.
     * <br>
     * This is used to store and generate data from the registry.
     *
     * @return registry used by the data type.
     */
    @NotNull Registry<T> registry();

    /**
     * The data type loading priority, higher priorities will be loaded first.
     *
     * @return loading priority.
     */
    int priority();

    /**
     * Load an entry; this is called for every element contained in every matching {@link #type() type} directory.
     * <br>
     * {@link #indexed() If indexed} this means this is only called to load the index files.
     *
     * @param entry entry to load.
     * @param key key of the entry.
     * @param ctx load context.
     *
     * @throws DataException if an error occurred while loading the entry.
     */
    void load(@NotNull DataEntry entry, @NotNull Key key, @NotNull Context<T> ctx) throws DataException;

    /**
     * Context used for deserializing and loading of the data type.
     *
     * @param <T> type of the data type that's using the context.
     */
    interface Context<T> {

        /**
         * Retrieve the registry which that should be used.
         *
         * @return registry.
         */
        @NotNull Registry<T> registry();

        /**
         * Open a new data entry.
         * <br>
         * The entry is limited to opening entries inside the same {@link DataType#type() type} as the data type using it.
         *
         * @return optional containing the data entry, if found, or an empty optional otherwise.
         */
        @NotNull Optional<DataEntry> openEntry(@NotNull Key key, @NotNull String extension);
    }
}
