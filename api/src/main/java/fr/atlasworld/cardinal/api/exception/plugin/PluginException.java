package fr.atlasworld.cardinal.api.exception.plugin;

import fr.atlasworld.cardinal.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

/**
 * Generic plugin exception.
 */
public class PluginException extends Exception {
    public final @NotNull String pluginName;
    public final @Nullable Plugin plugin;

    public PluginException(@NotNull String pluginName) {
        this.pluginName = pluginName;
        this.plugin = null;
    }

    public PluginException(@NotNull Plugin plugin) {
        this.pluginName = plugin.namespace();
        this.plugin = plugin;
    }

    public PluginException(String message, @NotNull Plugin plugin) {
        super(message);

        this.pluginName = plugin.namespace();
        this.plugin = plugin;
    }

    public PluginException(String message, @NotNull String pluginName) {
        super(message);

        this.pluginName = pluginName;
        this.plugin = null;
    }

    public PluginException(String message, Throwable cause, @NotNull Plugin plugin) {
        super(message, cause);

        this.pluginName = plugin.namespace();
        this.plugin = plugin;
    }

    public PluginException(String message, Throwable cause, @NotNull String pluginName) {
        super(message, cause);

        this.pluginName = pluginName;
        this.plugin = null;
    }

    public PluginException(Throwable cause, @NotNull Plugin plugin) {
        super(cause);

        this.pluginName = plugin.namespace();
        this.plugin = plugin;
    }

    public PluginException(Throwable cause, @NotNull String pluginName) {
        super(cause);

        this.pluginName = pluginName;
        this.plugin = null;
    }

    public PluginException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, @NotNull Plugin plugin) {
        super(message, cause, enableSuppression, writableStackTrace);

        this.pluginName = plugin.namespace();
        this.plugin = plugin;
    }

    public @NotNull String pluginName() {
        return this.pluginName;
    }

    public @Nullable Plugin plugin() {
        return plugin;
    }
}
