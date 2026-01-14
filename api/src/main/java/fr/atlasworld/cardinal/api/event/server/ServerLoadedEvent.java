package fr.atlasworld.cardinal.api.event.server;

import fr.atlasworld.cardinal.api.event.trait.ServerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called when the server finished (re)loading.
 * <br><br>
 * This is called after things such as datapacks or scripts have been (re)loaded.
 * If you need to work with entries that were loaded from datapacks, you can now safely use them.
 */
public final class ServerLoadedEvent implements ServerEvent {
    private final long loadTime;
    private final boolean reload;

    @ApiStatus.Internal
    public ServerLoadedEvent(long loadTime, boolean reload) {
        this.loadTime = loadTime;
        this.reload = reload;
    }

    /**
     * Retrieve the time the server took to reload.
     *
     * @return time the server took to reload.
     */
    public long serverReloadTime() {
        return this.loadTime;
    }

    /**
     * Retrieve whether the server was reloaded or if it was the initial load.
     *
     * @return {@code true} if the server was reloaded, {@code false} otherwise.
     */
    public boolean reload() {
        return this.reload;
    }
}
