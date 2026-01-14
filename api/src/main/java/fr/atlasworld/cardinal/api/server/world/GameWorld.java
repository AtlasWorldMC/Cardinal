package fr.atlasworld.cardinal.api.server.world;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Represents an available world.
 */
public interface GameWorld {

    /**
     * Retrieve the world revision.
     *
     * @return current world revision.
     */
    int revision();

    /**
     * Retrieve the authors of the world.
     *
     * @return authors of the world.
     */
    @NotNull Set<Component> authors();

    /**
     * Whether the underlying world is generated or not.
     *
     * @return {@code true} if the world is generated, {@code false} otherwise.
     */
    boolean generated();

    /**
     * Provide the world to the instance.
     * <br>
     * Used for the game world to properly provide a generator or a chunk-loader the provided instance.
     * <p>
     * <b>Warning:</b> Sharing chunk loaders or generators between instances is not recommended,
     * unless you know what you're doing.
     * <br>
     * Cardinal will handle world sharing if the games using it allow sharing.
     * <br>
     * Even if sharing chunk loader or generators may minimize the world's memory footprint, doing so may introduce
     * unexpected bugs.
     *
     * @param instance instance to provide the world to.
     * @param params   parameters defined by the map.
     * @throws Exception if an error occurs during the world setup.
     */
    void provide(@NotNull InstanceContainer instance, @Nullable CompoundBinaryTag params) throws Exception;
}
