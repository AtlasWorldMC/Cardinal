package fr.atlasworld.cardinal.api.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Server plugin manager api interface.
 */
public interface PluginManager {

    /**
     * Retrieve all loaded plugins.
     *
     * @return all loaded plugins as a set.
     */
    @NotNull Set<Plugin> loadedPlugins();

    /**
     * Retrieve a plugin by its identifier.
     *
     * @param id plugin identifier.
     * @return optional containing the requested plugin, or empty if no plugin with that identifier was found.
     */
    @NotNull Optional<Plugin> retrievePlugin(@NotNull String id);

    /**
     * Checks whether a plugin an identifier is loaded or not.
     *
     * @param identifier identifier of the plugin to check.
     * @return {@code true} if the plugin is loaded, {@code false} otherwise.
     */
    boolean isPluginLoaded(@NotNull String identifier);
}
