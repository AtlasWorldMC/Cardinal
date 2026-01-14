package fr.atlasworld.cardinal.plugin.provider;

import fr.atlasworld.cardinal.api.exception.plugin.PluginInitialLoadException;
import fr.atlasworld.cardinal.bootstrap.LaunchArguments;
import fr.atlasworld.cardinal.plugin.GroupClassLoader;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import fr.atlasworld.cardinal.plugin.PluginStore;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.util.List;

public final class CmdPluginProvider implements PluginProvider {
    private static final Logger LOGGER = Logging.logger();

    @Override
    public void loadPlugins(@NotNull PluginStore store, @NotNull GroupClassLoader groupLoader) {
        List<File> candidates = LaunchArguments.addedPlugins();
        if (candidates == null || candidates.isEmpty())
            return;

        for (File candidate : candidates) {
            try {
                PluginClassLoader loader = new PluginClassLoader(candidate.toPath(), groupLoader);
                store.registerPlugin(loader);
            } catch (PluginInitialLoadException ex) {
                LOGGER.error("{}: ", ex.pluginName(), ex);
            }
        }
    }
}
