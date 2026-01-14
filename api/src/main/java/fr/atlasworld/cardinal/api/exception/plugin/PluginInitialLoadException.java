package fr.atlasworld.cardinal.api.exception.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Called when a plugin fails to load,
 */
public class PluginInitialLoadException extends PluginException {
    public PluginInitialLoadException(@NotNull String pluginName, @NotNull String message, Throwable ex) {
        super(message, ex, pluginName);
    }

    public PluginInitialLoadException(@NotNull String pluginName, @NotNull String message) {
        super(message, pluginName);
    }
}
