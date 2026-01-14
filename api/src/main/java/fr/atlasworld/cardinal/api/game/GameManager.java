package fr.atlasworld.cardinal.api.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Server Game Manager.
 */
public interface GameManager {

    /**
     * Create a new {@link Game.Builder} to create a new game.
     *
     * @return newly created game builder.
     */
    @NotNull Game.Builder gameBuilder();

    /**
     * Retrieve all currently registered games.
     *
     * @return a set of all registered games.
     */
    @NotNull Set<Game> games();

    /**
     * Retrieve a game by its identifier.
     *
     * @param identifier identifier of the game to retrieve.
     * @return optional containing the requested game, or empty if no game with that identifier was found.
     */
    @NotNull Optional<Game> retrieveGame(@NotNull Key identifier);

    /**
     * Retrieve all currently registered maps.
     *
     * @return a set of all registered maps.
     */
    @NotNull Set<GameMap> maps();

    /**
     * Retrieve a map by its identifier.
     *
     * @param identifier identifier of the map to retrieve.
     * @return optional containing the requested map, or empty if no map with that identifier was found.
     */
    @NotNull Optional<GameMap> retrieveMap(@NotNull Key identifier);

    /**
     * Retrieve all currently active games.
     *
     * @return a set of all active games.
     */
    @NotNull Set<GameContainer> activeGames();

    /**
     * Retrieve a game by its identifier.
     *
     * @param identifier identifier of the game to retrieve.
     * @return optional containing the requested game, or empty if no game with that identifier was found.
     */
    @NotNull Optional<GameContainer> retrieveGameContainer(@NotNull UUID identifier);

    /**
     * Request the manager to create a new game.
     *
     * @param game game to create.
     * @param map  map the game should use.
     * @return newly created game.
     */
    @NotNull GameContainer createGame(@NotNull Game game, @NotNull GameMap map);

    /**
     * Define the player redirector.
     *
     * @param redirector player redirector.
     */
    void setPlayerRedirector(@NotNull PlayerRedirector redirector);

    /**
     * Retrieve the player redirector being used by the server.
     *
     * @return player redirector that the server is using.
     */
    @NotNull PlayerRedirector playerRedirector();

    /**
     * Redirects a player to an available game.
     *
     * @param player player to redirect.
     * @param disconnect whether the player should be disconnected if no game is available.
     *
     * @return {@code true} if the player was redirected, {@code false} otherwise, if {@code disconnect} is {@code true} then this caused the player to be disconnected.
     */
    CompletableFuture<Boolean> redirect(@NotNull Player player, boolean disconnect);

    /**
     * Retrieve the associated game container for an instance.
     *
     * @param instance instance linked to the game container.
     * @return optional containing the game container, or an empty optional if the instance isn't linked to any game.
     */
    Optional<GameContainer> retrieveAssociatedContainer(@NotNull Instance instance);

    /**
     * Helper method used to quickly retrieve the {@link GameContainer} in which the player is currently in.
     *
     * @param player player.
     * @return optional containing the game container, or an empty optional if the player isn't currently in a game.
     */
    static Optional<GameContainer> getPlayerContainer(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null!");
        Preconditions.checkArgument(player instanceof CardinalPlayer, "Player is not a CardinalPlayer!");

        return ((CardinalPlayer) player).gameContainer();
    }

    /**
     * Helper method used to quickly check whether the player is a spectator or not.
     *
     * @param player player to check for.
     * @return {@code true} if the player is a spectator, {@code false} otherwise.
     */
    static boolean isPlayerSpectator(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player cannot be null!");
        Preconditions.checkArgument(player instanceof CardinalPlayer, "Player is not a CardinalPlayer!");

        return ((CardinalPlayer) player).isGameSpectator();
    }
}
