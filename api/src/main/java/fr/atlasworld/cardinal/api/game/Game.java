package fr.atlasworld.cardinal.api.game;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.kyori.adventure.text.Component;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface Game {

    /**
     * Retrieve the game display name.
     *
     * @return game display name.
     */
    @NotNull Component displayName();

    /**
     * Request the manager to create a new game.
     *
     * @param map map the game should use.
     * @return newly created game.
     */
    @NotNull GameContainer create(@NotNull GameMap map);

    /**
     * Whether the game can share its maps with other games.
     * <p>
     * Map sharing can be powerful by lowering the resource usage for games.
     * But this also means that world states are shared,
     * as such adding or removing a block from one game will result in the other game's maps to be affected.
     * <br>
     * Use this carefully.
     *
     * @return {@code true} if the game can share its maps with other games, {@code false} otherwise.
     */
    boolean canShareMap();

    /**
     * Retrieve the dimension type from a world identifier.
     *
     * @param identifier identifier of the dimension.
     * @return minestom registry key containing the dimension type.
     */
    @NotNull RegistryKey<@NotNull DimensionType> dimension(@NotNull String identifier);

    /**
     * Create a new builder.
     *
     * @return newly created builder.
     */
    static @NotNull Builder builder() {
        return CardinalServer.getServer().gameManager().gameBuilder();
    }

    /**
     * Game Builder
     */
    interface Builder {

        /**
         * Set the game display name.
         *
         * @param displayName game display name.
         * @return builder instance.
         */
        @NotNull Builder displayName(@NotNull Component displayName);

        /**
         * Game logic supplier, executed everytime a {@link GameContainer} is created and attaches the newly create {@link GameLogic} to it.
         *
         * @param supplier {@link GameLogic} builder.
         * @return builder instance.
         */
        @NotNull Builder logic(@NotNull Supplier<GameLogic> supplier);

        /**
         * Set the plugin responsible for the game.
         *
         * @param plugin plugin responsible for the game.
         * @return builder instance.
         */
        @NotNull Builder plugin(@NotNull Plugin plugin);

        /**
         * Whether the game can share maps with other games.
         * <p>
         * Map sharing can be powerful by lowering the resource usage for games.
         * But this also means that world states are shared,
         * as such adding or removing a block from one game will result in the other game's maps to be affected.
         *
         * @param canShareMap true if maps can be shared, false otherwise.
         * @return builder instance.
         */
        @NotNull Builder canShareMap(boolean canShareMap);

        /**
         * Adds a dimension to the game.
         * <p>
         * Games should at least have one dimension.
         *
         * @param dimension dimension key should match one of the keys inside {@link GameMap},
         *                  if not the game creation will fail as the game asks for a dimension that the {@link GameMap}
         *                  doesn't have
         * @param type      dimension type.
         * @return builder instance.
         */
        @NotNull Builder dimension(@NotNull String dimension, @NotNull RegistryHolder<DimensionType> type);

        /**
         * Build the game instance.
         *
         * @return newly created game.
         */
        @NotNull Game build();
    }
}
