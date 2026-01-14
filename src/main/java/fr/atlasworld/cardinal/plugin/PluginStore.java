package fr.atlasworld.cardinal.plugin;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.exception.plugin.PluginInitialLoadException;
import fr.atlasworld.cardinal.api.exception.plugin.PluginInitializationException;
import fr.atlasworld.cardinal.api.exception.plugin.PluginLoadException;
import fr.atlasworld.cardinal.api.exception.plugin.PluginUnloadException;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.plugin.provider.CmdPluginProvider;
import fr.atlasworld.cardinal.plugin.provider.FilePluginProvider;
import fr.atlasworld.cardinal.plugin.provider.PluginProvider;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public final class PluginStore {
    private static final Logger LOGGER = Logging.logger();
    private static final List<PluginProvider> PROVIDERS = List.of(
            new CmdPluginProvider(),
            new FilePluginProvider()
    );

    private final GroupClassLoader groupLoader;
    private final Map<String, PluginClassLoader> plugins;

    public PluginStore() {
        this.groupLoader = new GroupClassLoader(CardinalServer.class.getClassLoader());
        this.plugins = new HashMap<>();
    }

    public void registerPlugin(PluginClassLoader pluginLoader) {
        if (this.plugins.containsKey(pluginLoader.meta().identifier())) {
            LOGGER.error("Multiple plugins with the identifier '{}' we're found, only one of them will be loaded.", pluginLoader.meta().identifier());
            return; // Prevent registering
        }

        this.plugins.put(pluginLoader.meta().identifier(), pluginLoader);
        LOGGER.info("Found plugin '{}'.", pluginLoader.meta().identifier());
    }

    public Optional<Plugin> retrievePlugin(@NotNull String identifier) {
        return Optional.ofNullable(this.plugins.get(identifier)).map(PluginClassLoader::plugin);
    }

    public Set<Plugin> plugins() {
        return this.plugins.values().stream()
                .map(PluginClassLoader::plugin)
                .filter(Objects::nonNull)
                .map(plugin -> (Plugin) plugin)
                .collect(Collectors.toSet());
    }

    public GroupClassLoader groupClassLoader() {
        return this.groupLoader;
    }

    public void scanPlugins() {
        PROVIDERS.forEach(provider -> {
            provider.loadPlugins(this, this.groupLoader);
        });
    }

    public void initializePlugins() {
        var plugins = this.plugins.values().iterator();

        while (plugins.hasNext()) {
            PluginClassLoader loader = plugins.next();
            try {
                loader.initiatePlugin();
                this.groupLoader.registerPluginClassLoader(loader);
            } catch (PluginInitializationException e) {
                LOGGER.error("Failed to initialize plugin '{}': {}", e.pluginName(), e.getMessage());
                this.unloadBrokenPlugin(loader);
            }
        }
    }

    public void loadPlugins() {
        for (PluginClassLoader loader : this.plugins.values()) {
            try {
                loader.loadPacks();

                try {
                    loader.plugin().load();
                } catch (Throwable ex) {
                    throw new PluginLoadException(loader.plugin(), ex);
                }
            } catch (PluginLoadException | PluginInitialLoadException e) {
                LOGGER.error("{}:", e.pluginName(), e);
            }
        }
    }

    public void unloadPlugins() {
        for (PluginClassLoader loader : this.plugins.values()) {
            try {
                try {
                    loader.plugin().unload();
                } catch (Throwable ex) {
                    throw new PluginUnloadException(loader.plugin(), ex);
                }
            } catch (PluginUnloadException e) {
                LOGGER.error("{}:", e.pluginName(), e);
            }
        }
    }

    private void unloadBrokenPlugin(PluginClassLoader loader) {
        this.plugins.remove(loader.meta().identifier());

        try {
            loader.close();
        } catch (Throwable ex) {
            LOGGER.error("Failed to gracefully close plugin file '{}': {}", loader.source().getFileName(), ex.getMessage());
        }
    }
}
