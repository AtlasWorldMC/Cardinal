package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when registering {@link DataTypeOld data types}.
 */
public final class DataTypeRegistrationEvent extends RegistrationEvent<DataTypeOld<?, ?>> {
    @ApiStatus.Internal
    public DataTypeRegistrationEvent(@NotNull Registry<DataTypeOld<?, ?>> registry) {
        super(registry);
    }
}
