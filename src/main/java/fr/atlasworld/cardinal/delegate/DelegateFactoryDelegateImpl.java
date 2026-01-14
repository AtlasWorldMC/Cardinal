package fr.atlasworld.cardinal.delegate;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.delegate.*;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.util.ReflectionUtils;

public final class DelegateFactoryDelegateImpl implements DelegateFactoryDelegate {
    public static final DelegateFactoryDelegate INSTANCE = new DelegateFactoryDelegateImpl();
    public static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static void init() {
        try {
            ReflectionUtils.staticInject(DelegateFactory.class, "delegate", INSTANCE);
        } catch (ReflectiveOperationException ex) {
            Main.crash("Failed to inject DelegateFactoryDelegate into DelegateFactory", ex);
        }
    }

    private final ItemDelegateImpl itemDelegate;
    private final BlockDelegateImpl blockDelegate;
    private final EnchantmentDelegateImpl enchantmentDelegate;
    private final RegistryDelegateImpl registryDelegate;

    private DelegateFactoryDelegateImpl() {
        this.itemDelegate = new ItemDelegateImpl();
        this.blockDelegate = new BlockDelegateImpl();
        this.enchantmentDelegate = new EnchantmentDelegateImpl();
        this.registryDelegate = new RegistryDelegateImpl();
    }

    @Override
    public ItemDelegate itemDelegate() {
        this.validateInternalClass();
        return this.itemDelegate;
    }

    @Override
    public BlockDelegate blockDelegate() {
        this.validateInternalClass();
        return this.blockDelegate;
    }

    @Override
    public EnchantmentDelegate enchantmentDelegate() {
        this.validateInternalClass();
        return this.enchantmentDelegate;
    }

    @Override
    public RegistryDelegate registryDelegate() {
        this.validateInternalClass();
        return this.registryDelegate;
    }

    private void validateInternalClass() {
        Class<?> callerClass = STACK_WALKER.getCallerClass();
        Preconditions.checkState(!CardinalPluginManager.isPluginClass(callerClass), "Illegal access to internal api: " + callerClass.getName());
    }
}
