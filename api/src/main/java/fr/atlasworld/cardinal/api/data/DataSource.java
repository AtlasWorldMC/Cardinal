package fr.atlasworld.cardinal.api.data;

import fr.atlasworld.cardinal.api.util.Priority;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a source for data-driven resources.
 */
public interface DataSource {

    /**
     * Retrieve all the data entries within the source.
     *
     * @return data entries
     */
    @NotNull Set<DataEntry> dataEntries();

    /**
     * Data Source priority, higher priority means that elements it won't be overridden by entries of lower priorities.
     *
     * @return Data Source priority.
     */
    default int priority() {
        return Priority.NORMAL.priority();
    }

    /**
     * Checks whether an entry is available inside the data source.
     *
     * @param path full path of the entry.
     * @return {@code true} if the entry is present, {@code false} otherwise.
     */
    boolean dataEntryPresent(@NotNull String path);

    /**
     * Retrieve a specific entry.
     *
     * @param path full path of the entry.
     * @return optional containing the entry, or an empty optional if the entry could not be found.
     */
    Optional<DataEntry> dataEntry(@NotNull String path);
}
