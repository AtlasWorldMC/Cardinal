package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.item.ItemProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the server is registering {@link ItemProvider ItemProviders}.
 */
public final class ItemProviderRegistrationEvent extends RegistrationEvent<ItemProvider> {
    @ApiStatus.Internal
    public ItemProviderRegistrationEvent(@NotNull Registry<ItemProvider> registry) {
        super(registry);
    }
}
