package fr.atlasworld.cardinal.api.server.world;

import net.hollowcube.schem.Schematic;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Represents a pool containing multiple structures, used by generators to access a specific set of structures, set by data packs.
 */
public interface StructurePool {

    /**
     * Resolve a random structure from the pool.
     *
     * @return random schematic of the pool.
     */
    @NotNull Schematic resolve();

    /**
     * Resolve a random structure from the pool.
     *
     * @param random random generator to use to determine the structure.
     *
     * @return random schematic of the pool.
     */
    @NotNull Schematic resolve(@NotNull Random random);

    /**
     * Retrieve a specific structure from the pool.
     *
     * @param key key of the structure to retrieve.
     *
     * @return optional containing the structure if present, empty otherwise.
     */
    Optional<Schematic> retrieveStructure(@NotNull Key key);

    /**
     * Retrieve all the structures in the pool.
     * <br><br>
     * Returns only the key, not the actual structure.
     * Doing otherwise would ask to load a massive amount of data into memory, only load what we need.
     *
     * @return keys of all the structures in the pool.
     */
    @NotNull Set<Key> structures();
}
