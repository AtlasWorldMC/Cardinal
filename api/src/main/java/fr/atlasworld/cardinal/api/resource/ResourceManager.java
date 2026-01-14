package fr.atlasworld.cardinal.api.resource;

import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.fresco.processor.ResourceProcessor;
import fr.atlasworld.fresco.source.EntryType;
import net.kyori.adventure.resource.ResourcePackInfo;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * General ResourceManager interface handles everything related to resource packs.
 */
public interface ResourceManager {

    /**
     * Check whether the embedded web server is enabled.
     *
     * @return {@code true} if the embedded web server is enabled, {@code false} otherwise.
     */
    boolean isEmbeddedWebServerEnabled();

    /**
     * Register a resource processor.
     * <br>
     * Resource processors allow modifying the entries of a resource pack during generation.
     * <br><br>
     * <b>Important:</b> These processors are only called when Cardinal is generating the resource packs.
     * In the development environment, cardinal generates the resource packs as such the processors will be called.
     * <br>
     * However, in production the <a href="https://github.com/AtlasWorldMC/Fresco">Fresco Gradle Plugin</a> is
     * responsible for generating the resource packs through the CI/CD pipeline, so you should register your processors to it to.
     * <br>
     * One last thing, cardinal's default processors are already registered by default.
     * <br><br>
     * <b>Warning:</b> This should be called at plugin initialization to ensure it's registered before the resource packs are generated.
     *
     * @param type      type of resource to the processor applies to.
     * @param processor resource processor.
     */
    void registerResourceProcessor(@NotNull EntryType type, @NotNull ResourceProcessor processor);

    /**
     * Create resource pack info for the specified plugin.
     *
     * @param plugin plugin to create the resource pack info for.
     * @return completable future containing the resource pack info.
     */
    CompletableFuture<ResourcePackInfo> createPackInfo(@NotNull Plugin plugin);
}
