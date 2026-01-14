package fr.atlasworld.cardinal.resource.entity;

import fr.atlasworld.fresco.source.ResourceEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Represents a source for resource pack assets.
 */
public interface ResourceSource {

    /**
     * Retrieve all resource entries within the source.
     *
     * @return resource entries
     */
    @NotNull Set<ResourceEntry> resourceEntries();

    /**
     * Checks whether an entry is available inside the resource source.
     *
     * @param path full path of the entry.
     * @return {@code true} if the entry is present, {@code false} otherwise.
     */
    boolean resourceEntryPresent(@NotNull String path);

    /**
     * Retrieve a specific entry.
     *
     * @param path full path of the entry.
     * @return optional containing the entry, or an empty optional if the entry could not be found.
     */
    @NotNull Optional<ResourceEntry> resourceEntry(@NotNull String path);
}
