package fr.atlasworld.cardinal.api.data;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Cardinal Data Manager.
 */
public interface DataManager {

    /**
     * Retrieve all loaded datapacks, plugins and builtin included.
     *
     * @return all loaded datapacks.
     */
    @NotNull Set<Datapack> loadedDatapacks();
}
