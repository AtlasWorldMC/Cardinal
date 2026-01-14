package fr.atlasworld.cardinal.api.event.trait;

import fr.atlasworld.cardinal.api.registry.Registry;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any event called about registering.
 *
 * @param <T> type of the registry.
 */
public interface RegistryEvent<T> extends Event {

    /**
     * Registry associated with the event.
     *
     * @return registry associated with the event.
     */
    @NotNull Registry<T> registry();
}
