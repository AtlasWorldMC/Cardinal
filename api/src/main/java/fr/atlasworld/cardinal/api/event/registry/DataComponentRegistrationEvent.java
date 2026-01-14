package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.component.ServerDataComponent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the server is registering {@link ServerDataComponent server-side data components}.
 */
public final class DataComponentRegistrationEvent extends RegistrationEvent<ServerDataComponent<?>> {
    @ApiStatus.Internal
    public DataComponentRegistrationEvent(@NotNull Registry<ServerDataComponent<?>> registry) {
        super(registry);
    }
}
