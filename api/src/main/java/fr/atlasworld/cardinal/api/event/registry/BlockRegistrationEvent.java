package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the server is registering blocks.
 */
public final class BlockRegistrationEvent extends RegistrationEvent<CardinalBlock> {
    @ApiStatus.Internal
    public BlockRegistrationEvent(@NotNull Registry<CardinalBlock> registry) {
        super(registry);
    }
}
