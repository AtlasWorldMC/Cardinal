package fr.atlasworld.cardinal.api.registry;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a registry that can be reloaded.
 *
 * @param <T> type of the registry.
 */
public interface ReloadableRegistry<T> extends Registry<T> {

    /**
     * Reload the registry.
     * <br>
     * This should not be called directly, and pass it to cardinal to handle it instead,
     * manually calling this method even when the plugin is reloading, will never cleanly handle the re-freezing of the registry.
     * <br>
     * <b>Warning:</b> the thread calling this method should be the same one that calls the {@link #freezeRegistry()} method,
     * if not done the method will throw an {@link IllegalMonitorStateException}.
     */
    @ApiStatus.Internal
    void reload();
}
