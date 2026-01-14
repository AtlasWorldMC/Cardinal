package fr.atlasworld.cardinal.event.player;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.game.PlayerRedirector;
import fr.atlasworld.cardinal.api.server.CardinalGameRules;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.game.GameContainerImpl;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerGameModeRequestEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class PlayerLifecycleHandler {
    private static final Logger LOGGER = Logging.logger();

    public static void handlePlayerConfiguration(@NotNull AsyncPlayerConfigurationEvent event) {
        Player player = event.getPlayer();
        LOGGER.info("Player {}({}): {} joined the server.", player.getUsername(), player.getUuid(), player.getPlayerConnection().getRemoteAddress());

        try {
            PlayerRedirector.Result result = CardinalServer.getServer().gameManager().playerRedirector().redirect(player);
            Preconditions.checkNotNull(result, "PlayerRedirector returned null result!");

            GameContainerImpl container = (GameContainerImpl) result.container();
            boolean spectator = result.spectator();
            Component disconnectMessage = result.disconnectMessage();

            if (container != null && container.state().isJoinable()) {
                LOGGER.debug("Adding '{}' to game '{}'.", player.getUsername(), container.identifier());
                boolean added = container.addPlayer(event, spectator);

                if (!added) {
                    LOGGER.warn("Disconnecting player '{}', Game container '{}' could not accept the player.", player.getUsername(), container.identifier());
                    player.kick(Component.text("Could not join game.", NamedTextColor.RED));
                }

                return;
            }

            if (disconnectMessage != null) {
                LOGGER.info("Disconnecting player '{}': {}", player.getUsername(), Serializers.ANSI.serialize(disconnectMessage));
                player.kick(disconnectMessage);
                return;
            }

            LOGGER.error("Failed to handle redirector for '{}': Redirector did not return any result.", player.getUsername());
            player.kick(Component.text("Internal server error, please contact an administrator.", NamedTextColor.RED));
        } catch (Throwable ex) {
            LOGGER.error("Failed to handle redirector for '{}':", player.getUsername(), ex);
            player.kick(Component.text("Internal server error, please contact an administrator.", NamedTextColor.RED));
            return;
        }
    }

    public static void onGameModeRequest(PlayerGameModeRequestEvent event) {
        event.getPlayer().setGameMode(event.getRequestedGameMode());
    }

    public static void onPlayerMove(PlayerMoveEvent event) {
        CardinalPlayer player = (CardinalPlayer) event.getPlayer();
        player.gameContainer().ifPresent(container -> {
            Point delta = event.getNewPosition().sub(player.getPosition());

            if (delta.y() > 0 && player.isOnGround()) {
                double exhaustMultiplier = container.getRuleValueOrDefault(CardinalGameRules.JUMP_EXHAUSTION_MULTIPLIER.get(), 1D);
                float exhaustion = (float) (player.isSprinting() ? (exhaustMultiplier * 0.2f) : (exhaustMultiplier * 0.5f));
                player.addExhaustion(exhaustion);
            }

            if (player.isOnGround()) {
                int length = (int) Math.round(Math.sqrt(delta.x() * delta.x() + delta.z() * delta.z()) * 100);
                float exhaustion = (player.isSprinting() ? 0.1f : 0.0f) * (float) length * 0.01f;

                if (length > 0)
                    player.addExhaustion(exhaustion);
            }

            // TODO: Add fluid exhaustion handling.
        });
    }

    public static void handlePlayerDisconnect(PlayerDisconnectEvent event) {
        Player player = event.getPlayer();
        LOGGER.info("Player {}({}): {} left the server.", player.getUsername(), player.getUuid(), player.getPlayerConnection().getRemoteAddress());
    }
}
