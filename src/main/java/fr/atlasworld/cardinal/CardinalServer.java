package fr.atlasworld.cardinal;

import com.google.common.base.Stopwatch;
import fr.atlasworld.cardinal.api.command.CommandManager;
import fr.atlasworld.cardinal.api.data.DataManager;
import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.event.server.ServerLoadedEvent;
import fr.atlasworld.cardinal.api.event.server.ServerStartedEvent;
import fr.atlasworld.cardinal.api.game.GameManager;
import fr.atlasworld.cardinal.api.plugin.PluginManager;
import fr.atlasworld.cardinal.api.resource.ResourceManager;
import fr.atlasworld.cardinal.api.server.ServerMode;
import fr.atlasworld.cardinal.bootstrap.BuildInfo;
import fr.atlasworld.cardinal.bootstrap.LaunchArguments;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.command.CardinalCommandManager;
import fr.atlasworld.cardinal.configuration.ServerConfiguration;
import fr.atlasworld.cardinal.data.CardinalDataManager;
import fr.atlasworld.cardinal.data.gen.DataGenerationManager;
import fr.atlasworld.cardinal.delegate.DelegateFactoryDelegateImpl;
import fr.atlasworld.cardinal.event.ServerListener;
import fr.atlasworld.cardinal.game.CardinalGameManager;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.profiler.CardinalProfilerManager;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.resource.CardinalResourceManager;
import fr.atlasworld.cardinal.server.entity.CardinalPlayerImpl;
import fr.atlasworld.cardinal.util.Logging;
import fr.atlasworld.cardinal.util.ReflectionUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class CardinalServer extends fr.atlasworld.cardinal.api.CardinalServer {
    public static final String SERVER_BRAND = "Cardinal";

    private static final Logger LOGGER = Logging.logger();
    private static CardinalServer instance;

    private final MinecraftServer server;
    private final ServerConfiguration configuration;

    private final CardinalPluginManager pluginManager;
    private final CardinalDataManager dataManager;
    private final CardinalResourceManager resourceManager;
    private final CardinalGameManager gameManager;
    private final CardinalCommandManager commandManager;
    private final CardinalProfilerManager sparkProfiler;

    private final ServerMode mode;

    public CardinalServer() {
        this.initiateApi(); // Update instances everywhere
        this.mode = this.determineMode();

        this.configuration = new ServerConfiguration();
        this.configuration.apply();

        this.server = MinecraftServer.init(ServerConfiguration.Authentication.determineAuth(this.configuration));

        this.pluginManager = new CardinalPluginManager();
        this.dataManager = new CardinalDataManager(this, this.pluginManager);
        this.resourceManager = new CardinalResourceManager(this.configuration);
        this.gameManager = new CardinalGameManager(this.configuration);
        this.commandManager = new CardinalCommandManager();
        this.sparkProfiler = new CardinalProfilerManager(this.pluginManager);

        MinecraftServer.getConnectionManager().setPlayerProvider(CardinalPlayerImpl::new); // Set minestom to use our implementation of the player class.
    }

    public void initialize() {
        Logging.logMultiline(LOGGER, Logging.ASCII.formatted(BuildInfo.version(), MinecraftServer.VERSION_NAME), Level.INFO);
        LOGGER.info("Initializing server...");

        MinecraftServer.setBrandName(SERVER_BRAND);
        CardinalRegistries.initialize();
        ServerListener.initialize();

        this.pluginManager.initialize();
        if (this.mode == ServerMode.DATA_GENERATION)
            return; // Nothing more needs to be initialized for data generation.

        this.gameManager.initialize();
        this.commandManager.initialize();
    }

    public synchronized void load(boolean reload) {
        if (this.mode == ServerMode.DATA_GENERATION) {
            this.handleDataGeneration();
            return;
        }

        Stopwatch watch = Stopwatch.createStarted();
        LOGGER.info(reload ? "Reloading server..." : "Loading server...");

        this.pluginManager.load(reload);
        CardinalRegistries.load(reload);

        this.dataManager.load(reload);
        this.resourceManager.load(reload);

        CardinalRegistries.freezeRegistries();

        this.gameManager.load(reload);
        this.commandManager.load(reload);
        watch.stop();

        LOGGER.info("Server loaded in {}ms.", watch.elapsed().toMillis());
        try {
            EventDispatcher.call(new ServerLoadedEvent(watch.elapsed(TimeUnit.MICROSECONDS), reload));
        } catch (Throwable ex) {
            LOGGER.error("Failed to pass server loaded event:", ex);
        }
    }

    private void handleDataGeneration() {
        LOGGER.info("Starting data generation...");
        Stopwatch watch = Stopwatch.createStarted();

        DataGenerationManager manager = new DataGenerationManager(this.pluginManager);
        manager.initialize();
        manager.generate();

        watch.stop();
        LOGGER.info("Data Generation done in {}ms.", watch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void start() {
        if (this.mode == ServerMode.DATA_GENERATION)
            return;

        LOGGER.info("Starting server...");

        this.sparkProfiler.start();
        this.server.start(this.configuration.serverAddress(), this.configuration.serverPort());

        long startTime = System.currentTimeMillis() - Main.START_TIME;
        LOGGER.info("Ready! Server started in {}ms. Listening on: [{}:{}]", startTime, this.configuration.serverAddress(), this.configuration.serverPort());

        this.commandManager.start(); // Make sure to start the console thread after the server is fully initialized.

        try {
            EventDispatcher.call(new ServerStartedEvent(startTime));
        } catch (Throwable ex) {
            LOGGER.error("Failed to pass server started event:", ex);
        }
    }

    public void shutdown(boolean interrupt) {
        LOGGER.info("Shutting down server...");

        if (this.resourceManager != null)
            this.resourceManager.shutdown(interrupt);

        if (this.commandManager != null)
            this.commandManager.shutdown(interrupt); // Shutdown the command manager first, to prevent any commands from being executed while some services are dead.

        if (this.sparkProfiler != null)
            this.sparkProfiler.shutdown(interrupt);

        if (this.pluginManager != null)
            this.pluginManager.shutdown(interrupt);

        if (this.server != null && !interrupt)
            MinecraftServer.stopCleanly();

        if (!interrupt)
            LOGGER.info("Server stopped, Good Bye! :)");
    }

    @Override
    public @NotNull PluginManager pluginManager() {
        return this.pluginManager;
    }

    @Override
    public @NotNull DataManager dataManager() {
        return this.dataManager;
    }

    @Override
    public @NotNull ResourceManager resourceManager() {
        return this.resourceManager;
    }

    @Override
    public @NotNull GameManager gameManager() {
        return this.gameManager;
    }

    @Override
    public @NotNull CommandManager commandManager() {
        return this.commandManager;
    }

    @Override
    public ServerMode serverMode() {
        return this.mode;
    }

    public ServerMode determineMode() {
        if (LaunchArguments.dataGen() != null)
            return ServerMode.DATA_GENERATION;

        if (LaunchArguments.devMode())
            return ServerMode.DEVELOPMENT;

        return ServerMode.PRODUCTION;
    }

    @Override
    public @NotNull Component name() {
        return Component.text(BuildInfo.name() + " (Builtin)");
    }

    @Override
    public @NotNull Component description() {
        return Component.text(BuildInfo.description());
    }

    @Override
    public @NotNull String version() {
        return BuildInfo.version();
    }

    @Override
    public @NotNull Set<Component> authors() {
        return BuildInfo.authors().stream().map(Component::text).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public static ServerConfiguration configuration() {
        return instance().configuration;
    }

    public static CardinalServer instance() {
        return instance;
    }

    private void initiateApi() {
        if (instance != null)
            throw new IllegalStateException("CardinalServer instance already exists.");

        instance = this;

        try {
            ReflectionUtils.staticInject(Class.forName("fr.atlasworld.cardinal.api.CardinalServer"), "instance", this);
        } catch (Throwable ex) {
            Main.crash("Failed to inject Cardinal server instance", ex);
        }

        DelegateFactoryDelegateImpl.init();
    }

    @Override
    public @NotNull DataSource source() {
        return this.dataManager.source(this);
    }
}
