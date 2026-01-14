package fr.atlasworld.cardinal.resource;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.resource.ResourceManager;
import fr.atlasworld.cardinal.configuration.ServerConfiguration;
import fr.atlasworld.cardinal.resource.server.EmbeddedResourceServer;
import fr.atlasworld.cardinal.resource.server.ResourceServerFactory;
import fr.atlasworld.cardinal.util.Logging;
import fr.atlasworld.fresco.processor.ResourceProcessor;
import fr.atlasworld.fresco.source.EntryType;
import net.kyori.adventure.resource.ResourcePackInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class CardinalResourceManager implements ResourceManager {
    private static final Logger LOGGER = Logging.logger();

    private final ResourceServer resourceServer;

    public static CompletableFuture<ResourcePackInfo> retrievePackInfo(@NotNull Plugin plugin) {
        return CardinalServer.instance().resourceManager().createPackInfo(plugin);
    }

    public CardinalResourceManager(ServerConfiguration configuration) {
        this.resourceServer = ResourceServerFactory.createResourceServer(configuration);
    }

    public void load(boolean reload) {
        this.resourceServer.load(reload);
    }

    public void shutdown(boolean interrupt) {
        if (interrupt && !(this.resourceServer instanceof EmbeddedResourceServer)) // Don't interrupt embedded web server.
            return;

        this.resourceServer.cleanup();
    }

    @Override
    public boolean isEmbeddedWebServerEnabled() {
        return this.resourceServer instanceof EmbeddedResourceServer;
    }

    @Override
    public void registerResourceProcessor(@NotNull EntryType type, @NotNull ResourceProcessor processor) {
        if (this.resourceServer instanceof EmbeddedResourceServer embeddedServer) {
            embeddedServer.registerResourceProcessor(type, processor);
            return;
        }

        if (!CardinalServer.isProduction())
            LOGGER.warn("Attempted to register a resource processor while not using an embedded web server.");
    }

    @Override
    public CompletableFuture<ResourcePackInfo> createPackInfo(@NotNull Plugin plugin) {
        return this.resourceServer.retrievePackInfo(plugin);
    }
}
