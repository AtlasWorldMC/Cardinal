package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when worlds are getting registered.
 */
public final class WorldRegistrationEvent extends RegistrationEvent<GameWorld> {
    @ApiStatus.Internal
    public WorldRegistrationEvent(@NotNull Registry<GameWorld> registry) {
        super(registry);
    }
}
