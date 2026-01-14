package fr.atlasworld.cardinal.api.exception.plugin;

import org.jetbrains.annotations.NotNull;

/**
 * Thrown when the initial constructor of the plugin fails.
 */
public class PluginInitializationException extends PluginException {
    public PluginInitializationException(String message, @NotNull String pluginName) {
        super(message, pluginName);
    }

    public PluginInitializationException(String message, Throwable cause, @NotNull String pluginName) {
        super(message, cause, pluginName);
    }
}
