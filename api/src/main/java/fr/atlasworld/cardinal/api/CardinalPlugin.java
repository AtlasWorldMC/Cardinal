package fr.atlasworld.cardinal.api;

import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.data.Meta;
import fr.atlasworld.cardinal.api.event.server.ServerLoadedEvent;
import fr.atlasworld.cardinal.api.exception.plugin.PluginLoadException;
import fr.atlasworld.cardinal.api.exception.plugin.PluginUnloadException;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.plugin.PluginContext;
import fr.atlasworld.cardinal.api.plugin.internal.PluginLoader;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;

import java.util.Set;

/**
 * Represents an abstract base for creating plugins running on the Cardinal server.
 * All plugins must extend this class to integrate with Cardinal's plugin system.
 */
public abstract class CardinalPlugin implements Plugin {
    private final PluginContext ctx;
    private final Meta meta;

    public CardinalPlugin() {
        if (!(this.getClass().getClassLoader() instanceof PluginLoader loader))
            throw new IllegalStateException("CardinalPlugins must be loaded using a PluginClassLoader!");

        this.ctx = loader.context();
        this.meta = loader.meta();
    }

    @Override
    public final @NotNull String namespace() {
        return this.ctx.identifier();
    }

    @Override
    public final @NotNull Component name() {
        return this.meta.name();
    }

    @Override
    public final @NotNull Component description() {
        return this.meta.description();
    }

    public final @NotNull String version() {
        return this.meta.version();
    }

    @Override
    public final @NotNull Set<Component> authors() {
        return this.meta.authors();
    }

    @ApiStatus.Internal
    public void load() throws PluginLoadException {
        this.load(this.ctx);
    }

    @ApiStatus.Internal
    public void unload() throws PluginUnloadException {
        this.unload(this.ctx);
    }

    /**
     * Retrieve the plugin logger.
     *
     * @return plugin logger.
     */
    protected final @NotNull Logger pluginLogger() {
        return this.ctx.logger();
    }

    /**
     * Called when the plugin is loaded, and you can safely use Cardinal's API.
     *
     * @param ctx plugin context.
     */
    protected abstract void load(@NotNull PluginContext ctx) throws PluginLoadException;

    /**
     * Called when the server reloads, here you should execute logic that could change and that needs a reload to be updated.
     * (ex: configuration files, data changes ect...)
     *
     * @param ctx plugin context.
     * @deprecated listen for {@link ServerLoadedEvent} instead, this method is no longer called.
     */
    @Deprecated(forRemoval = true)
    protected void reload(@NotNull PluginContext ctx) {
    }

    /**
     * Called when the server shutdowns, here you should gracefully terminate any running services.
     *
     * @param ctx plugin context.
     */
    protected abstract void unload(@NotNull PluginContext ctx) throws PluginUnloadException;

    /**
     * Retrieve the data source of the plugin, note this should not be called at init as datapacks aren't yet loaded
     *
     * @return the data source of the plugin.
     */
    @Override
    public final @UnknownNullability DataSource source() {
        return ((PluginLoader) this.getClass().getClassLoader()).dataSource();
    }
}
