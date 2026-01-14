package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when registering {@link GameWorldFormat game world format}.
 */
public final class WorldFormatRegistrationEvent extends RegistrationEvent<GameWorldFormat> {
    @ApiStatus.Internal
    public WorldFormatRegistrationEvent(@NotNull Registry<GameWorldFormat> registry) {
        super(registry);
    }
}
