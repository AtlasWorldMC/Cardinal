package fr.atlasworld.cardinal.api.event.trait;

import fr.atlasworld.cardinal.api.CardinalServer;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any event called about the server.
 */
public interface ServerEvent extends Event {

    /**
     * Retrieve the server instance.
     *
     * @return server instance.
     */
    default @NotNull CardinalServer server() {
        return CardinalServer.getServer();
    }
}
