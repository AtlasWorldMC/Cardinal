package fr.atlasworld.cardinal.resource.server;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.bootstrap.LaunchArguments;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.configuration.ServerConfiguration;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import fr.atlasworld.cardinal.resource.ResourceServer;
import fr.atlasworld.cardinal.resource.server.embedded.ResourcePackCache;
import fr.atlasworld.cardinal.util.Logging;
import fr.atlasworld.fresco.FrescoProcessor;
import fr.atlasworld.fresco.processor.ResourceProcessor;
import fr.atlasworld.fresco.source.EntryType;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.util.JavalinException;
import net.kyori.adventure.resource.ResourcePackInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class EmbeddedResourceServer implements ResourceServer {
    private static final Logger LOGGER = Logging.logger();
    private static final File RESOURCE_PACK_CACHE = new File(".cache/resourcepacks");

    static {
        // Shut the fuck up.
        Logging.disableLogger("io.javalin");
        Logging.disableLogger("org.eclipse.jetty");

        if (!RESOURCE_PACK_CACHE.isDirectory())
            if (!RESOURCE_PACK_CACHE.mkdirs())
                Main.crash("Failed to create resource pack cache directory, please check file permissions.");
    }

    private final Map<EntryType, Set<ResourceProcessor>> processors;
    private final Map<Plugin, ResourcePackInfo> packs;

    private final ServerConfiguration configuration;
    private final Javalin server;
    private final ResourcePackCache cache;

    public EmbeddedResourceServer(ServerConfiguration configuration) {
        this.processors = new ConcurrentHashMap<>();
        this.packs = new ConcurrentHashMap<>();

        this.configuration = configuration;
        this.server = Javalin.create(this::configureServer);
        this.cache = new ResourcePackCache();
    }

    private void configureServer(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.useVirtualThreads = true;

        config.staticFiles.add(RESOURCE_PACK_CACHE.getPath(), Location.EXTERNAL);
    }

    @Override
    public @NotNull String name() {
        return "Embedded Web Server";
    }

    @Override
    public void load(boolean reload) {
        if (reload)
            return;

        try {
            this.cache.load();
        } catch (IOException e) {
            Main.crash("Failed to load resource pack cache index:", e);
        }

        this.generatePacks();

        LOGGER.info("Starting embedded web server...");
        this.server.start(this.configuration.embeddedWebServerAddress(), this.configuration.embeddedWebServerPort());
    }

    private void generatePacks() {
        for (Plugin plugin : CardinalServer.instance().pluginManager().loadedPlugins()) {
            try {
                PluginClassLoader loader = (PluginClassLoader) plugin.getClass().getClassLoader();
                this.generatePack(loader);
            } catch (IOException e) {
                LOGGER.error("Failed to generate pack for plugin '{}'", plugin.namespace(), e);
            }
        }
    }

    private void generatePack(PluginClassLoader loader) throws IOException {
        LOGGER.info("Starting pack generation for plugin '{}'..", loader.meta().identifier());
        File output = this.determineOutputFile(loader);

        if (this.cache.isCached(loader, output) && !LaunchArguments.skipCache()) {
            LOGGER.info("Pack for plugin '{}' is already cached, skipping...", loader.meta().identifier());
            this.producePackInfo(loader, output);

            return;
        }

        FrescoProcessor.Builder builder = FrescoProcessor.create();
        builder.logger(LOGGER).meta(loader.meta().asPackMeta()).outputFile(output);
        builder.addEntries(loader.store().resourceEntries());

        this.processors.forEach(((type, resourceProcessors) ->
                builder.addProcessors(type, resourceProcessors.toArray(new ResourceProcessor[0]))));

        try (FrescoProcessor processor = builder.build()) {
            processor.process();
        } catch (Throwable ex) {
            LOGGER.error("Processor failed for '{}':", loader.meta().identifier(), ex);
            return;
        }

        LOGGER.info("Pack '{}' generated successfully.", loader.meta().identifier());
        this.cache.cache(loader, output);
        this.producePackInfo(loader, output);
    }

    private void producePackInfo(PluginClassLoader loader, File output) {
        String url = this.configuration.webServerBaseUrl().endsWith("/") ? this.configuration.webServerBaseUrl() + output.getName() :
                this.configuration.webServerBaseUrl() + "/" + output.getName();

        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(UUID.randomUUID(), URI.create(url), this.cache.retrievePackHash(loader, output).toString());
        this.packs.put(loader.plugin(), info);
    }

    private File determineOutputFile(PluginClassLoader loader) {
        return new File(RESOURCE_PACK_CACHE, loader.meta().identifier() + ".zip");
    }

    @Override
    public void cleanup() {
        try {
            this.server.stop();
        } catch (JavalinException ex) {
            // Shallow error, it could be fixed if javalin allowed join / wait on the jettyThread, but it doesn't allow it directly, we need to listen for events.
            // TODO: Fix this, should listen for Javalin events and until then simply wait.
            if (ex.getCause() instanceof InterruptedException)
                return;

            throw ex;
        }
    }

    @Override
    public @NotNull CompletableFuture<ResourcePackInfo> retrievePackInfo(@NotNull Plugin plugin, boolean bypassCache) {
        return CompletableFuture.completedFuture(this.packs.get(plugin));
    }

    public void registerResourceProcessor(@NotNull EntryType type, @NotNull ResourceProcessor processor) {
        this.processors.computeIfAbsent(type, key -> new HashSet<>()).add(processor);
    }
}
