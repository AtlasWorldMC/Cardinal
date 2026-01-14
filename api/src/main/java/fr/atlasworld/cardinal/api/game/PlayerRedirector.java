package fr.atlasworld.cardinal.api.game;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface provided to a {@link GameManager} to redirect player to game.
 * <br>
 * This is used when a player is connecting or that the player was connected to a game that finished or was interrupted.
 */
@FunctionalInterface
public interface PlayerRedirector {

    /**
     * Redirect a connecting player to a specific {@link GameContainer}.
     *
     * @param player incoming player.
     * @return resulting action to take on the player.
     */
    @NotNull
    Result redirect(@NotNull Player player);

    class Result {
        private final @Nullable GameContainer container;
        private final @Nullable Component disconnectMessage;
        private final boolean spectator;

        /**
         * Redirect result, sends the player to the set container.
         * <p>
         * Container state must be {@link GameContainer.State#isJoinable() joinable}.
         *
         * @param container container to redirect the player to.
         * @param spectator whether the player should be a spectator in the container.
         * @return result to the redirect.
         */
        public static @NotNull Result redirect(@NotNull GameContainer container, boolean spectator) {
            Preconditions.checkNotNull(container);
            Preconditions.checkArgument(container.state().isJoinable(), "Container must be joinable!");

            return new Result(container, spectator, null);
        }

        /**
         * Disconnect result will disconnect the player with the provided disconnection message.
         *
         * @param disconnectMessage disconnection message.
         * @return result to the redirect.
         */
        public static @NotNull Result disconnect(@NotNull Component disconnectMessage) {
            Preconditions.checkNotNull(disconnectMessage);
            return new Result(null, false, disconnectMessage);
        }

        @ApiStatus.Internal
        private Result(@Nullable GameContainer container, boolean spectator, @Nullable Component disconnectMessage) {
            this.container = container;
            this.disconnectMessage = disconnectMessage;
            this.spectator = spectator;
        }

        /**
         * Retrieve the container to send to.
         *
         * @return container to send the player to.
         */
        public @Nullable GameContainer container() {
            return this.container;
        }

        /**
         * Whether the player joining the container should be a spectator or not.
         *
         * @return {@code true} if the player should be a spectator, {@code false} otherwise.
         */
        public boolean spectator() {
            return this.spectator;
        }

        /**
         * Retrieve the disconnect message.
         *
         * @return disconnect message.
         */
        public @Nullable Component disconnectMessage() {
            return this.disconnectMessage;
        }

    }
}
