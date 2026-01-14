package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the server is registering items.
 */
public final class ItemRegistrationEvent extends RegistrationEvent<CardinalItem> {
    @ApiStatus.Internal
    public ItemRegistrationEvent(@NotNull Registry<CardinalItem> registry) {
        super(registry);
    }
}
