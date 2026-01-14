package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the server is registering enchantments.
 */
public final class EnchantmentRegistrationEvent extends RegistrationEvent<CardinalEnchantment> {
    @ApiStatus.Internal
    public EnchantmentRegistrationEvent(@NotNull Registry<CardinalEnchantment> registry) {
        super(registry);
    }
}
