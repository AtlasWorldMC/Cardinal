package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.event.game.GameStateChangeEvent;
import fr.atlasworld.cardinal.api.game.*;
import fr.atlasworld.cardinal.event.EventNodeFactory;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.resource.CardinalResourceManager;
import fr.atlasworld.cardinal.server.entity.CardinalPlayerImpl;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class GameContainerImpl implements GameContainer {
    private static final Logger LOGGER = Logging.logger();

    private final CardinalWorldManager worldManager;
    private final GameRuleStore store;

    private final UUID identifier;
    private final GameImpl game;
    private final GameMap map;
    private final GameLogic logic;

    private final Map<UUID, Instance> instances;
    private final GameContextImpl context;

    private volatile @Nullable ResourcePackRequest pack;
    private volatile @NotNull State state;
    private volatile @Nullable Throwable cause;

    public GameContainerImpl(CardinalWorldManager worldManager, UUID identifier, GameImpl game, GameMap map, GameLogic logic) {
        this.worldManager = worldManager;
        this.store = new GameRuleStore();

        this.identifier = identifier;
        this.game = game;
        this.map = map;
        this.logic = logic;

        this.instances = new HashMap<>();
        this.state = State.INITIALIZING;

        EventNode<@NotNull Event> node = EventNodeFactory.createGameNode(game, this);
        this.context = new GameContextImpl(this, node);
    }

    @Override
    public @NotNull UUID identifier() {
        return this.identifier;
    }

    @Override
    public @NotNull State state() {
        return this.state;
    }

    @Override
    public @NotNull Game game() {
        return this.game;
    }

    @Override
    public @NotNull GameMap map() {
        return this.map;
    }

    @Override
    public @NotNull GameLogic logic() {
        return this.logic;
    }

    @Override
    public @NotNull String displayName() {
        return CardinalRegistries.GAMES.retrieveKey(this.game).orElseThrow(() -> new IllegalStateException("Game from game container'" + this.identifier + "' is not registered!"))
                .value() + "-" + this.identifier.toString().substring(0, 5);
    }

    @Override
    public boolean interrupt() {
        return this.updateState(State.INTERRUPTED);
    }

    @Override
    public @NotNull Set<Instance> instances() {
        return Set.copyOf(this.instances.values());
    }

    @Override
    public boolean isInstanceLinked(@NotNull Instance instance) {
        return this.instances.containsValue(instance);
    }

    @Override
    public <T> @Nullable T getRuleValue(@NotNull GameRule<T> rule) {
        return this.store.get(rule);
    }

    @Override
    public <T> @NotNull T getRuleValueOrDefault(@NotNull GameRule<T> rule, @NotNull T fallback) {
        T value = this.store.get(rule);
        return value != null ? value : fallback;
    }

    @Override
    public <T> void setGameRule(@NotNull GameRule<T> rule, @NotNull T value) {
        this.store.set(rule, value);
    }

    public @NotNull GameRuleStore ruleStore() {
        return this.store;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> addPlayer(@NotNull Player player, boolean spectator) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkArgument(player instanceof CardinalPlayerImpl, "Unsupported player implementation.");

        if (!this.state.isJoinable() || !this.context.allowPlayerJoining())
            return CompletableFuture.completedFuture(false);

        if (this.context.joiningInstance() == null) {
            LOGGER.warn("Game logic '{}' did not set a joining instance, cannot add player to game.", this.logic.getClass().getName());
            return CompletableFuture.completedFuture(false);
        }

        CardinalPlayerImpl cardinalPlayer = (CardinalPlayerImpl) player;
        cardinalPlayer.resetPlayerState();
        cardinalPlayer.setGame(this, spectator);

        if (cardinalPlayer.getInstance() == this.context.joiningInstance())
            return CompletableFuture.completedFuture(true); // No need to change the instance of the player.

        LOGGER.info("Player '{}' joined game '{}'.", cardinalPlayer.getUsername(), this.identifier);
        return player.setInstance(this.context.joiningInstance(), Pos.ZERO).thenApply(unused -> {
            if (this.pack != null)
                cardinalPlayer.sendResourcePacks(this.pack);
            else
                cardinalPlayer.clearResourcePacks();

            return true;
        });
    }

    public boolean addPlayer(@NotNull AsyncPlayerConfigurationEvent event, boolean spectator) {
        if (!this.state.isJoinable() || !this.context.allowPlayerJoining())
            return false;

        if (this.context.joiningInstance() == null) {
            LOGGER.warn("Game logic '{}' did not set a joining instance, cannot add player to game.", this.logic.getClass().getName());
            return false;
        }

        CardinalPlayerImpl cardinalPlayer = (CardinalPlayerImpl) event.getPlayer();
        cardinalPlayer.setGame(this, spectator);

        event.setSpawningInstance(this.context.joiningInstance());
        if (this.pack != null)
            cardinalPlayer.sendResourcePacks(this.pack);

        LOGGER.info("Player '{}' joined game '{}'.", cardinalPlayer.getUsername(), this.identifier);
        return true;
    }

    public void tick() {
        switch (this.state) {
            case INITIALIZING -> this.handleInit();
            case WAITING -> this.handleWait();
            case RUNNING -> this.handleRunning();
            case FINISHED -> this.handleFinished();
            case INTERRUPTED -> this.handleInterrupted();
            default -> throw new IllegalStateException("Unknown state: " + this.state);
        }
    }

    @Override
    public Optional<Instance> retrieveInstance(@NotNull UUID identifier) {
        Preconditions.checkNotNull(identifier, "Identifier cannot be null");
        return Optional.ofNullable(this.instances.get(identifier));
    }

    @ApiStatus.Internal
    public void registerInstance(@NotNull Instance instance) {
        Preconditions.checkNotNull(instance, "Instance cannot be null");
        this.instances.put(instance.getUuid(), instance);
    }

    private void handleInit() {
        CompletableFuture<ResourcePackInfo> futurePack = this.game.plugin() == null ?
                CompletableFuture.completedFuture(null) : CardinalResourceManager.retrievePackInfo(this.game.plugin());

        try {
            this.logic.initialize(this.context);
            if (!CardinalServer.isProduction() && this.context.joiningInstance() == null)
                LOGGER.warn("Game logic '{}' did not set a joining instance after initialization.", this.logic.getClass().getName());

            if (!CardinalServer.isProduction() && !this.context.allowPlayerJoining())
                LOGGER.warn("Game logic '{}' did not allow player joining after initialization, if the game needs player to start it will soft-lock.", this.logic.getClass().getName());
        } catch (Throwable ex) {
            LOGGER.error("Failed to initialize logic for container '{}':", this.identifier, ex);
            this.cause = ex;
            this.updateState(State.INTERRUPTED);
        }

        ResourcePackInfo packInfo = futurePack.join();
        if (packInfo != null)
            this.pack = ResourcePackRequest.resourcePackRequest()
                    .required(true)
                    .replace(true)
                    .packs(futurePack.join())
                    .build();

        this.updateState(State.WAITING);
    }

    private void handleWait() {
        // TODO: Handle player waiting
        this.updateState(State.RUNNING);
    }

    private void handleRunning() {
        try {
            boolean shouldEnd = this.logic.update(this.context);

            if (shouldEnd)
                this.updateState(State.FINISHED);
        } catch (Throwable ex) {
            LOGGER.error("Failed pass update event to game logic '{}':", this.identifier, ex);
            this.cause = ex;
            this.updateState(State.INTERRUPTED);
        }
    }

    private void handleFinished() {
        try {
            this.logic.finish(this.context);
        } catch (Throwable ex) {
            LOGGER.error("Failed to pass finish event to game logic '{}':", this.identifier, ex);
        }

        this.handleCleanup();
    }

    private void handleInterrupted() {
        LOGGER.warn("Interrupted game container '{}'", this.identifier);

        try {
            this.logic.interrupt(this.context, this.cause);
        } catch (Throwable ex) {
            LOGGER.error("Failed to gracefully interrupt game logic '{}':", this.identifier, ex);
        }

        this.handleCleanup();
    }

    private void handleCleanup() {
        try {
            this.logic.terminate(this.context);
        } catch (Throwable ex) {
            LOGGER.error("Failed to gracefully terminate game '{}':", this.identifier, ex);
        }

        List<CompletableFuture<Void>> redirectFutures = new ArrayList<>();
        
        this.instances.values().forEach(instance -> {
            for (Player player : instance.getPlayers()) {
                CompletableFuture<Void> redirectFuture = CardinalServer.getServer().gameManager()
                        .redirect(player, true)
                        .thenApply(result -> null);
                redirectFutures.add(redirectFuture);
            }
        });

        CompletableFuture.allOf(redirectFutures.toArray(new CompletableFuture[0])).join();

        // Unregister instances
        this.instances.values().forEach(this.worldManager::unregisterInstance);
        this.instances.clear();
    }

    @CanIgnoreReturnValue
    private synchronized boolean updateState(State state) {
        // Make sure to not override the terminal states.
        if (this.state == state || this.state == State.INTERRUPTED || this.state == State.FINISHED)
            return false;

        State oldState = this.state;
        this.state = state;

        EventDispatcher.call(new GameStateChangeEvent(this, oldState));
        return true;
    }

    public @NotNull Instance createInstance(GameMap.MapWorld world) {
        Instance instance = this.worldManager.loadInstance(this, world);
        if (instance == null) // Let the calling method fail, if not handled by the game, this will end as the failing cause for the game.
            throw new IllegalStateException("World loading failed for world '" + world.dimension() + "' in game '" + this.identifier + "'");

        return instance;
    }

    public static class GameTask {
        private final CardinalGameManager manager;
        private final GameContainerImpl container;
        private final Task task;

        public static void schedule(@NotNull CardinalGameManager gameManager, @NotNull GameContainerImpl container) {
            Preconditions.checkNotNull(gameManager, "Game manager cannot be null");
            Preconditions.checkNotNull(container, "Game container cannot be null");

            Preconditions.checkArgument(container.state() == State.INITIALIZING, "Game container must be in INITIALIZING state to be scheduled!");
            new GameTask(gameManager, container);
        }

        private GameTask(CardinalGameManager manager, GameContainerImpl container) {
            this.manager = manager;
            this.container = container;

            this.task = MinecraftServer.getSchedulerManager().scheduleTask(this::run, TaskSchedule.nextTick(), TaskSchedule.nextTick());
        }

        private void run() {
            if (this.container.state() == State.INTERRUPTED || this.container.state() == State.FINISHED) {
                this.task.cancel();
                this.manager.unregisterGameContainer(this.container);
            }

            this.container.tick();
        }
    }
}
