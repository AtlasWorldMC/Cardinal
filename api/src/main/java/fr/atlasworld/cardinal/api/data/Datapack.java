package fr.atlasworld.cardinal.api.data;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a datapack loaded on cardinal.
 */
public interface Datapack extends Meta {

    /**
     * Retrieve the data source of this datapack.
     *
     * @return data source of this datapack.
     */
    @NotNull DataSource source();
}
