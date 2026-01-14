package fr.atlasworld.cardinal.api.delegate;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface DelegateFactoryDelegate {
    ItemDelegate itemDelegate();

    BlockDelegate blockDelegate();

    EnchantmentDelegate enchantmentDelegate();

    RegistryDelegate registryDelegate();
}
