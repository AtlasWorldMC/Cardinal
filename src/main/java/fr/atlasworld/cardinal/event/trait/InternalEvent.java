package fr.atlasworld.cardinal.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;

import java.util.Set;

/**
 * Marker interface for internal events that should not be passed down to the plugins.
 */
public interface InternalEvent {

    // Minestom events that should not be passed down to the plugins.
    Set<Class<? extends Event>> BLACKLISTED_EVENTS = Set.of(
            AsyncPlayerConfigurationEvent.class
    );

}
