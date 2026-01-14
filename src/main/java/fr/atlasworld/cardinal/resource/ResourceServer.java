package fr.atlasworld.cardinal.resource;

import fr.atlasworld.cardinal.api.plugin.Plugin;
import net.kyori.adventure.resource.ResourcePackInfo;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Abstract representation of a resource server, which handles all resources and requests.
 */
public interface ResourceServer {

    /**
     * Resource server name.
     *
     * @return name of the resource server.
     */
    @NotNull String name();

    /**
     * Loads the resource server.
     */
    void load(boolean reload);

    /**
     * Cleans up the resource server.
     */
    void cleanup();

    /**
     * Retrieve the resource pack info linked to the specified plugin.
     *
     * @param plugin plugin to which the resource pack is linked.
     * @return future containing the resource pack info.
     */
    default @NotNull CompletableFuture<ResourcePackInfo> retrievePackInfo(@NotNull Plugin plugin) {
        return retrievePackInfo(plugin, false);
    }

    /**
     * Retrieve the resource pack info linked to the specified plugin.
     *
     * @param plugin      plugin to which the resource pack is linked.
     * @param bypassCache whether to bypass the cache.
     * @return future containing the resource pack info.
     */
    @NotNull CompletableFuture<ResourcePackInfo> retrievePackInfo(@NotNull Plugin plugin, boolean bypassCache);
}
