package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.event.game.GameCreatedEvent;
import fr.atlasworld.cardinal.api.game.*;
import fr.atlasworld.cardinal.api.server.CardinalGames;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.configuration.ServerConfiguration;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CardinalGameManager implements GameManager {
    private static final Logger LOGGER = Logging.logger();

    private final CardinalWorldManager worldManager;
    private final Map<UUID, GameContainer> gameContainers;

    private @NotNull PlayerRedirector redirector;

    public CardinalGameManager(ServerConfiguration configuration) {
        this.worldManager = new CardinalWorldManager();
        this.gameContainers = new ConcurrentHashMap<>();
        this.redirector = new DummyRedirector();
    }

    public void initialize() {

    }

    public void load(boolean reload) {
        if (reload)
            return;

        if (!CardinalServer.isProduction()) {
            this.redirector = new DevRedirector(this);
            LOGGER.warn("Dev Feature: development environment and redirector have been loaded. IMPORTANT: In production you will need to specify a proper PlayerRedirector!");
        }
    }

    @Override
    public @NotNull Game.Builder gameBuilder() {
        return new GameImpl.BuilderImpl();
    }

    @Override
    public @NotNull Set<Game> games() {
        return CardinalRegistries.GAMES.values();
    }

    @Override
    public @NotNull Optional<Game> retrieveGame(@NotNull Key identifier) {
        return CardinalRegistries.GAMES.retrieveValue(identifier);
    }

    @Override
    public @NotNull Set<GameMap> maps() {
        return CardinalRegistries.MAPS.values();
    }

    @Override
    public @NotNull Optional<GameMap> retrieveMap(@NotNull Key identifier) {
        return CardinalRegistries.MAPS.retrieveValue(identifier);
    }

    @Override
    public @NotNull Set<GameContainer> activeGames() {
        return Set.copyOf(this.gameContainers.values());
    }

    @Override
    public @NotNull Optional<GameContainer> retrieveGameContainer(@NotNull UUID identifier) {
        Preconditions.checkNotNull(identifier, "Identifier cannot be null");
        return Optional.ofNullable(this.gameContainers.get(identifier));
    }

    public @NotNull CardinalWorldManager worldManager() {
        return this.worldManager;
    }

    @Override
    public @NotNull GameContainer createGame(@NotNull Game game, @NotNull GameMap map) {
        Preconditions.checkNotNull(game, "Game cannot be null");
        Preconditions.checkNotNull(map, "Map cannot be null");

        Preconditions.checkArgument(CardinalRegistries.GAMES.containsValue(game), "Game is not registered!");
        Preconditions.checkArgument(CardinalRegistries.MAPS.containsValue(map), "Map is not registered!");
        Preconditions.checkArgument(map.game().get() == game, "Map does not support the provided game!");
        Preconditions.checkArgument(game instanceof GameImpl, "Game must be an instance of GameImpl! Please use Game.Builder to create a your games.");

        final UUID identifier = UUID.randomUUID();
        final GameImpl impl = (GameImpl) game;
        final GameLogic logic = impl.supplyLogic();

        GameContainerImpl container = new GameContainerImpl(this.worldManager, identifier, impl, map, logic);
        this.gameContainers.put(identifier, container);
        GameContainerImpl.GameTask.schedule(this, container); // Handle the updating of the game every tick.
        EventDispatcher.call(new GameCreatedEvent(container));

        LOGGER.info("Created new game '{}' with id '{}'.", CardinalRegistries.GAMES.retrieveKey(game).get(), identifier);

        return container;
    }

    @Override
    public synchronized void setPlayerRedirector(@NotNull PlayerRedirector redirector) {
        Preconditions.checkNotNull(redirector, "PlayerRedirector cannot be null");

        if (!(this.redirector instanceof DummyRedirector || this.redirector instanceof DevRedirector))
            LOGGER.warn("PlayerRedirector conflict, PlayerRedirector has been set first to '{}', but now has been replaced by another PlayerRedirector '{}'.", this.redirector.getClass(), redirector.getClass());

        this.redirector = redirector;
    }

    @Override
    public @NotNull PlayerRedirector playerRedirector() {
        return this.redirector;
    }

    @Override
    public CompletableFuture<Boolean> redirect(@NotNull Player player, boolean disconnect) {
        try {
            PlayerRedirector.Result result = CardinalServer.getServer().gameManager().playerRedirector().redirect(player);
            Preconditions.checkNotNull(result, "PlayerRedirector returned null result!");

            GameContainerImpl container = (GameContainerImpl) result.container();
            boolean spectator = result.spectator();
            Component disconnectMessage = result.disconnectMessage();

            if (container != null && container.state().isJoinable()) {
                LOGGER.debug("Adding '{}' to game '{}'.", player.getUsername(), container.identifier());
                return container.addPlayer(player, spectator).thenApply(added -> {
                    if (disconnect && !added) {
                        LOGGER.warn("Disconnecting player '{}', Game container '{}' could not accept the player.", player.getUsername(), container.identifier());
                        player.kick(Component.text("Could not join game.", NamedTextColor.RED));
                    }

                    return added;
                });
            }

            if (!disconnect)
                return CompletableFuture.completedFuture(false);

            if (disconnectMessage != null) {
                LOGGER.info("Disconnecting player '{}': {}", player.getUsername(), Serializers.ANSI.serialize(disconnectMessage));
                player.kick(disconnectMessage);
                return CompletableFuture.completedFuture(false);
            }

            LOGGER.error("Failed to handle redirector for '{}': Redirector did not return any result.", player.getUsername());
            player.kick(Component.text("Internal server error, please contact an administrator.", NamedTextColor.RED));
            return CompletableFuture.completedFuture(false);
        } catch (Throwable ex) {
            LOGGER.error("Failed to handle redirector for '{}':", player.getUsername(), ex);

            if (disconnect)
                player.kick(Component.text("Internal server error, please contact an administrator.", NamedTextColor.RED));

            return CompletableFuture.completedFuture(false);
        }
    }

    @Override
    public Optional<GameContainer> retrieveAssociatedContainer(@NotNull Instance instance) {
        return this.worldManager.gameContainer(instance);
    }

    public void unregisterGameContainer(@NotNull GameContainer container) {
        Preconditions.checkNotNull(container, "Container cannot be null");
        this.gameContainers.remove(container.identifier());
    }

    private static final class DummyRedirector implements PlayerRedirector {
        @Override
        public @NotNull Result redirect(@NotNull Player player) {
            Logging.logMultiline(LOGGER,
                    """
                            <!>---------------[DUMMY REDIRECTOR]---------------<!>
                             | No proper game redirector has been specified.    |
                             |                                                  |
                             | Since Cardinal isn't currently running in        |
                             | development mode, no game is created by default. |
                             |                                                  |
                             | You need to use a plugin or implement your own   |
                             | redirection code logic, without it no game is    |
                             | created by default and cardinal cannot handle    |
                             | incoming players.                                |
                            <!>------------------------------------------------<!>
                            """, Level.ERROR);

            return Result.disconnect(Component.text("Unable to connect, please see the console for more details.", NamedTextColor.RED));
        }
    }

    private static final class DevRedirector implements PlayerRedirector {
        private final GameContainer testEnvContainer;

        private DevRedirector(GameManager manager) {
            this.testEnvContainer = manager.createGame(CardinalGames.TEST.get(), CardinalRegistries.MAPS.retrieveValue(CardinalGames.TEST.key()).orElseThrow(() -> new IllegalStateException("Missing test environment map!")));
        }

        @Override
        public @NotNull Result redirect(@NotNull Player player) {
            if (!this.testEnvContainer.state().isJoinable()) {
                LOGGER.warn("Test Environment isn't currently joinable. (Did it get interrupted or is it still starting ?)");
                return Result.disconnect(Component.text("Server is currently not joinable, please try again later.", NamedTextColor.RED));
            }

            return Result.redirect(this.testEnvContainer, false);
        }
    }
}
