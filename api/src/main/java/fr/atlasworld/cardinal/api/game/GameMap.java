package fr.atlasworld.cardinal.api.game;

import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Game Map, represents an object that wraps around {@link GameWorld} and adds additional meta-data such as names,
 * generators params, etc...
 */
public interface GameMap {

    /**
     * Retrieve the map name.
     *
     * @return map name.
     */
    @NotNull Component name();

    /**
     * Retrieve the map description.
     *
     * @return map description.
     */
    @NotNull Component description();

    /**
     * Retrieve the game for which this map was designed to work with.
     * <p>
     * This is used to ensure maps are only used with compatible games.
     *
     * @return the game this map was designed for
     */
    @NotNull RegistryHolder<Game> game();

    /**
     * Retrieve all worlds used by this map.
     *
     * @return a list of all worlds used by this map.
     */
    @NotNull List<MapWorld> worlds();

    /**
     * Represents the world and all of his parameters the map uses.
     */
    interface MapWorld {

        /**
         * Retrieve the game world associated with this map world.
         *
         * @return game world.
         */
        @NotNull RegistryHolder<GameWorld> world();

        /**
         * Retrieve the dimension of the game this world is associated with.
         *
         * @return dimension key of the game this world is associated with.
         */
        @NotNull String dimension();

        /**
         * Extra parameters to pass to the {@link GameWorld}.
         *
         * @return extra parameters to pass to the {@link GameWorld}.
         */
        @NotNull CompoundBinaryTag extraParams();
    }
}
