package fr.atlasworld.cardinal.api.exception.plugin;

import fr.atlasworld.cardinal.api.CardinalPlugin;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.plugin.PluginContext;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when a plugin fails to execute {@link CardinalPlugin#unload(PluginContext)}.
 */
public class PluginUnloadException extends PluginException {
    public PluginUnloadException(@NotNull Plugin plugin, Throwable cause) {
        super("Failed to gracefully unload plugin", cause, plugin);
    }

    public PluginUnloadException(@NotNull Plugin plugin) {
        super("Failed to gracefully unload plugin", plugin);
    }
}
