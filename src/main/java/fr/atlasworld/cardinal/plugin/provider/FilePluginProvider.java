package fr.atlasworld.cardinal.plugin.provider;

import fr.atlasworld.cardinal.api.exception.plugin.PluginInitialLoadException;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.plugin.GroupClassLoader;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import fr.atlasworld.cardinal.plugin.PluginStore;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;

public final class FilePluginProvider implements PluginProvider {
    private static final Logger LOGGER = Logging.logger();
    private static final File PLUGIN_DIRECTORY = new File("plugins");

    static {
        if (!PLUGIN_DIRECTORY.isDirectory())
            if (!PLUGIN_DIRECTORY.mkdirs())
                Main.crash("Failed to create plugin directory, please check file permissions.");
    }

    @Override
    public void loadPlugins(@NotNull PluginStore store, @NotNull GroupClassLoader groupLoader) {
        LOGGER.info("Searching for plugins...");

        File[] pluginFiles = PLUGIN_DIRECTORY.listFiles(file -> file.isFile() && file.getName().endsWith(".jar"));
        if (pluginFiles == null)
            return;

        for (File pluginCandidate : pluginFiles) {
            try {
                PluginClassLoader loader = new PluginClassLoader(pluginCandidate.toPath(), groupLoader);
                store.registerPlugin(loader);
            } catch (PluginInitialLoadException ex) {
                LOGGER.error("{}: ", ex.pluginName(), ex);
            }
        }
    }
}
