package fr.atlasworld.cardinal.api.game;

import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a currently running game.
 */
public interface GameContainer {

    /**
     * Identifier of the container.
     *
     * @return unique identifier of the container.
     */
    @NotNull UUID identifier();

    /**
     * Represents the current game state.
     *
     * @return current game state.
     */
    @NotNull State state();

    /**
     * Game currently running in the container.
     *
     * @return game currently running in the container.
     */
    @NotNull Game game();

    /**
     * Map currently used by the game.
     *
     * @return map used by the game.
     */
    @NotNull GameMap map();

    /**
     * Retrieve the game logic instance powering this game.
     * <br><br>
     * <b>Warning:</b> do not ever call the standard methods of the {@link GameLogic},
     * these should only be called by the container and calling these will result in undefined behavior.
     *
     * @return game logic instance powering this game.
     */
    @ApiStatus.Experimental
    @NotNull GameLogic logic();

    /**
     * Container display name, this is also used as argument value for the {@link fr.atlasworld.cardinal.api.command.argument.GameContainerArgument}.
     *
     * @return container display name.
     */
    @NotNull String displayName();

    /**
     * Interrupts the currently running game.
     *
     * @return {@code true} if the game has been interrupted, {@code false} otherwise.
     */
    boolean interrupt();

    /**
     * Retrieve all instances linked to this container.
     *
     * @return instance linked to this container.
     */
    @NotNull Set<Instance> instances();

    /**
     * Retrieve a specific instance.
     *
     * @param identifier unique key of the instance.
     * @return optional containing the instance or an empty optional if the instance with the key could not be found.
     */
    Optional<Instance> retrieveInstance(@NotNull UUID identifier);

    /**
     * Checks whether and instance is linked to this container.
     *
     * @param instance instance to check for.
     * @return {@code true} if the instance is linked, {@code false} otherwise.
     */
    boolean isInstanceLinked(@NotNull Instance instance);

    /**
     * Retrieve the game rule value for the specified rule.
     *
     * @param rule rule to retrieve.
     *
     * @return the value set, or the default value if no value was set, if no rule was set and the game rule doesn't have a default value, {@code null} is returned.
     * @param <T> type of the game rule.
     */
    <T> @Nullable T getRuleValue(@NotNull GameRule<T> rule);

    /**
     * Retrieve the game rule value for the specified rule or fallback on a default value.
     *
     * @param rule rule to retrieve.
     * @param fallback fallback value if no value was set.
     *
     * @return the value set, or the default value if no value was set, if no rule was set and the game rule doesn't have a default value,
     *         the {@code fallback} value is returned.
     * @param <T> type of the game rule.
     */
    <T> @NotNull T getRuleValueOrDefault(@NotNull GameRule<T> rule, @NotNull T fallback);

    /**
     * Sets the game rule value.
     *
     * @param rule rule to set the value for.
     * @param value value to set.
     *
     * @param <T> type of the game rule.
     */
    <T> void setGameRule(@NotNull GameRule<T> rule, @NotNull T value);

    /**
     * Add a player to the game.
     *
     * @param player    player to add.
     * @param spectator whether the player is a spectator or not.
     * @return future of the player being added to the game, it may fail if the game refused the player.
     */
    @NotNull CompletableFuture<Boolean> addPlayer(@NotNull Player player, boolean spectator);

    /**
     * Represents all possible state a game container can be.
     */
    enum State {

        /**
         * Game is initializing, and setting up everything for the next game.
         */
        INITIALIZING(false),

        /**
         * Game is waiting for players to join.
         * Until min player required for the game is reached.
         */
        WAITING(true),

        /**
         * Running the game is currently active and running.
         */
        RUNNING(true),

        /**
         * Game just finished.
         * <br><br>
         * This is a <b>terminal</b> state, so you <b>shouldn't</b> keep a reference to this container.
         */
        FINISHED(false),

        /**
         * The game was interrupted or finished in an unrecoverable state.
         * <br><br>
         * This is a <b>terminal</b> state, so you <b>shouldn't</b> keep a reference to this container.
         */
        INTERRUPTED(false);

        private final boolean joinable;

        State(boolean joinable) {
            this.joinable = joinable;
        }

        /**
         * Whether the state the game currently is joinable.
         *
         * @return {@code true} if the game is joinable, {@code false} otherwise.
         */
        public boolean isJoinable() {
            return this.joinable;
        }
    }
}
