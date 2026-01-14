package fr.atlasworld.cardinal.api.delegate;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DelegateFactory {
    private static DelegateFactoryDelegate delegate;

    private DelegateFactory() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static ItemDelegate itemDelegate() {
        return delegate.itemDelegate();
    }

    public static BlockDelegate blockDelegate() {
        return delegate.blockDelegate();
    }

    public static EnchantmentDelegate enchantmentDelegate() {
        return delegate.enchantmentDelegate();
    }

    public static RegistryDelegate registryDelegate() {
        return delegate.registryDelegate();
    }
}
