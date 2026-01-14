package fr.atlasworld.cardinal.api.delegate;

import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.event.registry.RegistrationEvent;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

@ApiStatus.Internal
public interface RegistryDelegate {
    <T> @NotNull Registry<T> cardinalRegistry(@NotNull String key, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier);
    <T> @NotNull Registry<T> minestomRegistry(@NotNull String key, @NotNull DynamicRegistry<@NotNull T> minestomRegistry, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier);
    <T> @NotNull Registry<T> dataRegistry(@NotNull String key, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier);

    @NotNull Registry<CardinalBlock> blockRegistry(@NotNull String key);
    @NotNull Registry<DataTypeOld<?, ?>> dataTypeRegistry(@NotNull String key);
    @NotNull Registry<CardinalEnchantment> enchantmentRegistry(@NotNull String key);

    <T> @NotNull Register<T> createInternalRegister(Class<? extends RegistrationEvent<T>> eventClass);

    // Internal class access - these should be used if the class they require are only available internally.

    // Data Types
    @NotNull DataTypeOld<?, GameMap> retrieveGameMapType();
    @NotNull DataTypeOld<?, GameWorld> retrieveGameWorldType();
    @NotNull DataTypeOld<?, StructurePool> retrieveStructureType();

    // World Formats
    @NotNull GameWorldFormat retrievePolarWorldFormat(); // Doesn't use any internal classes but uses the Polar Loader, which is only available internally to prevent polluting plugin devs classpath.

    // Game
    @NotNull Game buildInternalGame(@NotNull Game.Builder builder);
}
