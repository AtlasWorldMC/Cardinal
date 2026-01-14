package fr.atlasworld.cardinal.plugin;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.plugin.PluginManager;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

public class CardinalPluginManager implements PluginManager {
    private static final Logger LOGGER = Logging.logger();

    public static boolean isPluginClass(Class<?> clazz) {
        return clazz.getClassLoader() != CardinalServer.class.getClassLoader();
    }

    private final PluginStore store;

    public CardinalPluginManager() {
        this.store = new PluginStore();
    }

    public void initialize() {
        this.store.scanPlugins();
        this.store.initializePlugins();
    }

    public void load(boolean reload) {
        if (reload) // Plugins cannot be reloaded.
            return;

        this.store.loadPlugins();
    }

    public void shutdown(boolean interrupt) {
        // Prevent further plugin interaction.
        // This is made to be sure that plugins won't interact with the crashing component.
        if (interrupt)
            return;

        LOGGER.info("Unloading plugins...");
        this.store.unloadPlugins();
    }

    @Override
    public @NotNull Set<Plugin> loadedPlugins() {
        return this.store.plugins();
    }

    @Override
    public @NotNull Optional<Plugin> retrievePlugin(@NotNull String id) {
        Preconditions.checkNotNull(id, "Identifier cannot be null");
        return this.store.retrievePlugin(id);
    }

    @Override
    public boolean isPluginLoaded(@NotNull String identifier) {
        return this.retrievePlugin(identifier).isPresent();
    }

    public GroupClassLoader groupClassLoader() {
        return this.store.groupClassLoader();
    }
}
