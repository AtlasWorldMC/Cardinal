package fr.atlasworld.cardinal.api.event.server;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.trait.ServerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Called when the server is started.
 */
public final class ServerStartedEvent implements ServerEvent {
    private final long startTime;

    @ApiStatus.Internal
    public ServerStartedEvent(long startTime) {
        Preconditions.checkArgument(startTime > 0, "Start time must be greater than 0!");
        this.startTime = startTime;
    }

    /**
     * Retrieve the time the server took to start.
     *
     * @return time the server took to start.
     */
    public long serverStartTime() {
        return this.startTime;
    }
}
