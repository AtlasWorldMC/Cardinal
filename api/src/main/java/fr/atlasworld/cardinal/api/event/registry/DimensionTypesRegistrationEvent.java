package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when registering {@link DimensionType dimension types}.
 *
 * @deprecated Dimension types are now data-driven.
 */
@Deprecated(forRemoval = true)
public final class DimensionTypesRegistrationEvent extends RegistrationEvent<DimensionType> {
    @ApiStatus.Internal
    public DimensionTypesRegistrationEvent(@NotNull Registry<DimensionType> registry) {
        super(registry);
    }
}
