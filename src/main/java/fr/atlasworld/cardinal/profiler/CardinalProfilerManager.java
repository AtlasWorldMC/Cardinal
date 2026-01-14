package fr.atlasworld.cardinal.profiler;

import fr.atlasworld.cardinal.api.data.Meta;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.command.builtin.ProfilerCommand;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.profiler.tick.CardinalTickHook;
import fr.atlasworld.cardinal.profiler.tick.CardinalTickReporter;
import fr.atlasworld.cardinal.profiler.world.CardinalWorldInfoProvider;
import fr.atlasworld.cardinal.util.Logging;
import fr.atlasworld.cardinal.util.ReflectionUtils;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.SparkPlugin;
import me.lucko.spark.common.command.sender.CommandSender;
import me.lucko.spark.common.monitor.ping.PlayerPingProvider;
import me.lucko.spark.common.platform.PlatformInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;
import me.lucko.spark.common.sampler.source.SourceMetadata;
import me.lucko.spark.common.tick.TickHook;
import me.lucko.spark.common.tick.TickReporter;
import me.lucko.spark.common.util.SparkThreadFactory;
import me.lucko.spark.common.util.classfinder.ClassFinder;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.ServerSender;
import net.minestom.server.entity.Player;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CardinalProfilerManager implements SparkPlugin {
    private static final Logger LOGGER = Logging.logger();

    public static final CardinalPlatformInfo PLATFORM_INFO = new CardinalPlatformInfo();
    public static final Path PROFILER_DIRECTORY = Path.of("profiler");

    private final CardinalPluginManager pluginManager;
    private final ScheduledExecutorService scheduler;
    private final SparkPlatform platform;

    public CardinalProfilerManager(CardinalPluginManager pluginManager) {
        this.pluginManager = pluginManager;

        this.scheduler = Executors.newScheduledThreadPool(1, new SparkThreadFactory());
        this.platform = new SparkPlatform(this);

        MinecraftServer.getCommandManager().register(new ProfilerCommand(this.platform));
    }

    public void start() {
        this.platform.enable();
    }

    public void shutdown(boolean interrupt) {
        this.platform.disable();

        if (interrupt)
            this.scheduler.shutdownNow();
        else
            this.scheduler.shutdown();
    }

    @Override
    public String getVersion() {
        return PLATFORM_INFO.getVersion();
    }

    @Override
    public Path getPluginDirectory() { // Represents the space where spark will output its files
        return PROFILER_DIRECTORY;
    }

    @Override
    public String getCommandName() {
        return "profiler spark";
    }

    @Override
    public Stream<? extends CommandSender> getCommandSenders() {
        return Stream.concat(
                MinecraftServer.getConnectionManager().getOnlinePlayers().stream(),
                Stream.of(MinecraftServer.getCommandManager().getConsoleSender(), this.retrieveConsoleSender())
        ).map(SparkCommandSenderAdapter::new);
    }

    private ServerSender retrieveConsoleSender() {
        try {
            return (ServerSender) ReflectionUtils.retrieveFieldValue(MinecraftServer.getCommandManager(), "serverSender");
        } catch (ReflectiveOperationException ex) {
            Main.crash("Failed to retrieve console sender", ex);
            return null;
        }
    }

    @Override
    public void executeAsync(Runnable runnable) {
        this.scheduler.execute(runnable);
    }

    @Override
    public PlatformInfo getPlatformInfo() {
        return PLATFORM_INFO;
    }

    @Override
    public void log(Level level, String message) {
        if (level.intValue() >= 1000) {
            LOGGER.error(message);
        } else if (level.intValue() >= 900) {
            LOGGER.warn(message);
        } else {
            LOGGER.info(message);
        }
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level.intValue() >= 1000) {
            LOGGER.error(message, throwable);
        } else if (level.intValue() >= 900) {
            LOGGER.warn(message, throwable);
        } else {
            LOGGER.info(message, throwable);
        }
    }

    @Override
    public ClassSourceLookup createClassSourceLookup() {
        return new CardinalClassSourceLookup();
    }

    @Override
    public ClassFinder createClassFinder() {
        return this.pluginManager.groupClassLoader().asClassFinder();
    }

    @Override
    public PlayerPingProvider createPlayerPingProvider() {
        return () -> MinecraftServer.getConnectionManager()
                .getOnlinePlayers().stream()
                .collect(Collectors.toMap(Player::getUsername, Player::getLatency));
    }

    @Override
    public Collection<SourceMetadata> getKnownSources() {
        return SourceMetadata.gather(
                this.pluginManager.loadedPlugins(), Plugin::namespace, Meta::version,
                plugin -> String.join(", ", plugin.authors().stream().map(Serializers.PLAIN_TEXT::serialize).toList()),
                plugin -> Serializers.PLAIN_TEXT.serialize(plugin.description())
        );
    }

    @Override
    public TickReporter createTickReporter() {
        return new CardinalTickReporter();
    }

    @Override
    public TickHook createTickHook() {
        return new CardinalTickHook();
    }

    @Override
    public WorldInfoProvider createWorldInfoProvider() {
        return new CardinalWorldInfoProvider();
    }
}
