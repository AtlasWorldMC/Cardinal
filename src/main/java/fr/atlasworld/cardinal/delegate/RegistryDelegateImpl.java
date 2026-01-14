package fr.atlasworld.cardinal.delegate;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.BlockRegistrationEvent;
import fr.atlasworld.cardinal.api.event.registry.DataTypeRegistrationEvent;
import fr.atlasworld.cardinal.api.event.registry.EnchantmentRegistrationEvent;
import fr.atlasworld.cardinal.api.event.registry.RegistrationEvent;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.registry.DataRegistry;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import fr.atlasworld.cardinal.data.type.StructureDataType;
import fr.atlasworld.cardinal.data.type.WorldDataTypeOld;
import fr.atlasworld.cardinal.event.EventNodeFactory;
import fr.atlasworld.cardinal.game.GameImpl;
import fr.atlasworld.cardinal.game.LoadedGameMap;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.registry.*;
import fr.atlasworld.cardinal.server.world.format.PolarWorldFormat;
import net.minestom.server.event.EventNode;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static fr.atlasworld.cardinal.registry.CardinalRegistries.NAMESPACE;

public final class RegistryDelegateImpl implements RegistryDelegate {
    public static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private static EventNode<@NotNull RegistrationEvent> REGISTER_EVENT_NODE;

    @Override
    public @NotNull <T> Registry<T> cardinalRegistry(@NotNull String key, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier) {
        Preconditions.checkNotNull(key, "Key must not be null");

        this.validateInternalClass();

        CardinalRegistry<T> registry = new CardinalRegistry<>(new RegistryKey(NAMESPACE, key));
        CardinalRegistries.REGISTRIES.registerRegistry(registry, eventSupplier);

        return registry;
    }

    @Override
    public @NotNull <T> Registry<T> minestomRegistry(@NotNull String key, @NotNull DynamicRegistry<@NotNull T> minestomRegistry, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier) {
        Preconditions.checkNotNull(key, "Key must not be null");
        Preconditions.checkNotNull(minestomRegistry, "MinestomRegistry must not be null");

        this.validateInternalClass();

        MinestomBackedRegistry<T> registry = new MinestomBackedRegistry<>(new RegistryKey(NAMESPACE, key), minestomRegistry);
        CardinalRegistries.REGISTRIES.registerRegistry(registry, eventSupplier);

        return registry;
    }

    @Override
    public @NotNull <T> Registry<T> dataRegistry(@NotNull String key, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier) {
        Preconditions.checkNotNull(key, "Key must not be null");

        this.validateInternalClass();

        DataRegistry<T> dataRegistry = new DataRegistry<>(new RegistryKey(NAMESPACE, key));
        CardinalRegistries.REGISTRIES.registerRegistry(dataRegistry, eventSupplier);

        return dataRegistry;
    }

    @Override
    public @NotNull Registry<CardinalBlock> blockRegistry(@NotNull String key) {
        Preconditions.checkNotNull(key, "Key must not be null");

        this.validateInternalClass();

        BlockRegistry registry = new BlockRegistry(new RegistryKey(NAMESPACE, key));
        CardinalRegistries.REGISTRIES.registerRegistry(registry, BlockRegistrationEvent::new);

        return registry;
    }

    @Override
    public @NotNull Registry<DataTypeOld<?, ?>> dataTypeRegistry(@NotNull String key) {
        Preconditions.checkNotNull(key, "Key must not be null");

        this.validateInternalClass();

        DataTypeRegistry registry = new DataTypeRegistry(new RegistryKey(NAMESPACE, key));
        CardinalRegistries.REGISTRIES.registerRegistry(registry, DataTypeRegistrationEvent::new);

        return registry;
    }

    @Override
    public @NotNull Registry<CardinalEnchantment> enchantmentRegistry(@NotNull String key) {
        Preconditions.checkNotNull(key, "Key must not be null");

        this.validateInternalClass();

        EnchantmentRegistry registry = new EnchantmentRegistry(new RegistryKey(NAMESPACE, key));
        CardinalRegistries.REGISTRIES.registerRegistry(registry, EnchantmentRegistrationEvent::new);

        return registry;
    }

    @Override
    public @NotNull <T> Register<T> createInternalRegister(Class<? extends RegistrationEvent<T>> eventClass) {
        Register<T> register = new Register<>(NAMESPACE);
        registerEventNode().addListener(eventClass, register::register);
        return register;
    }

    @Override
    public @NotNull DataTypeOld<?, GameMap> retrieveGameMapType() {
        return DataTypeOld.direct("map", LoadedGameMap.CODEC, CardinalRegistries.MAPS, map -> map);
    }

    @Override
    public @NotNull DataTypeOld<?, GameWorld> retrieveGameWorldType() {
        return WorldDataTypeOld.INSTANCE;
    }

    public @NotNull DataTypeOld<?, StructurePool> retrieveStructureType() {
        return StructureDataType.INSTANCE;
    }

    @Override
    public @NotNull GameWorldFormat retrievePolarWorldFormat() {
        return new PolarWorldFormat();
    }

    @Override
    public @NotNull Game buildInternalGame(Game.@NotNull Builder builder) {
        return ((GameImpl.BuilderImpl) builder).buildWithoutGame();
    }

    public static @NotNull EventNode<@NotNull RegistrationEvent> registerEventNode() {
        if (REGISTER_EVENT_NODE == null)
            REGISTER_EVENT_NODE = EventNodeFactory.createRegisterNode();

        return REGISTER_EVENT_NODE;
    }

    private void validateInternalClass() {
        Class<?> callerClass = STACK_WALKER.getCallerClass();
        Preconditions.checkState(!CardinalPluginManager.isPluginClass(callerClass), "Illegal access to internal api: " + callerClass.getName());
    }
}
