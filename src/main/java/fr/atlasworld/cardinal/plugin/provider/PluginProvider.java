package fr.atlasworld.cardinal.plugin.provider;

import fr.atlasworld.cardinal.plugin.GroupClassLoader;
import fr.atlasworld.cardinal.plugin.PluginStore;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin providers, handles the loading of plugins into their {@link fr.atlasworld.cardinal.plugin.PluginClassLoader}
 * and registers them to the {@link PluginStore}.
 */
@FunctionalInterface
public interface PluginProvider {
    void loadPlugins(@NotNull PluginStore store, @NotNull GroupClassLoader groupLoader);
}
