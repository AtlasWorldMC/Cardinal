package fr.atlasworld.cardinal.api.exception.plugin;

import fr.atlasworld.cardinal.api.CardinalPlugin;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.plugin.PluginContext;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a plugin fails to execute {@link CardinalPlugin#load(PluginContext)}.
 */
public class PluginLoadException extends PluginException {
    public PluginLoadException(@NotNull Plugin plugin, Throwable cause) {
        super("Failed to load plugin", cause, plugin);
    }

    public PluginLoadException(@NotNull Plugin plugin) {
        super("Failed to load plugin", plugin);
    }
}
