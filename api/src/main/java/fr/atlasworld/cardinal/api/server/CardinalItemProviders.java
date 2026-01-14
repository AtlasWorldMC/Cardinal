package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.ItemProviderRegistrationEvent;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.item.*;
import org.jetbrains.annotations.ApiStatus;

public final class CardinalItemProviders {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<ItemProvider> PROVIDERS = DELEGATE.createInternalRegister(ItemProviderRegistrationEvent.class);

    public static RegistryHolder<ItemProvider> BASIC = of("basic", CardinalItem.class);
    public static RegistryHolder<ItemProvider> BLOCK = of("block_item", CardinalBlockItem.class);
    public static RegistryHolder<ItemProvider> SHIELD = of("shield", CardinalShieldItem.class);
    public static RegistryHolder<ItemProvider> FOOD = of("food", CardinalFoodItem.class);
    public static RegistryHolder<ItemProvider> WEAPON = of("weapon", CardinalWeaponItem.class);

    private static RegistryHolder<ItemProvider> of(String name, Class<? extends CardinalItem> itemClass) {
        return PROVIDERS.register(name, () -> ItemProvider.basicProvider(itemClass));
    }

    private CardinalItemProviders() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
