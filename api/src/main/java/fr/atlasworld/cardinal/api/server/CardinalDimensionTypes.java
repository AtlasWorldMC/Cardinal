package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static fr.atlasworld.cardinal.api.registry.CardinalRegistries.NAMESPACE;

/**
 * Represents all the {@link DimensionType}s registered by cardinal.
 */
public final class CardinalDimensionTypes {
    public static final RegistryHolder<DimensionType> OVERWORLD = ofMinecraft("overworld");
    public static final RegistryHolder<DimensionType> OVERWORLD_CAVES = ofMinecraft("overworld_caves");
    public static final RegistryHolder<DimensionType> THE_NETHER = ofMinecraft("the_nether");
    public static final RegistryHolder<DimensionType> THE_END = ofMinecraft("the_end");

    public static final RegistryHolder<DimensionType> LIMBO = of("limbo");

    private CardinalDimensionTypes() {}

    private static RegistryHolder<DimensionType> of(@NotNull String name) {
        return CardinalRegistries.DIMENSION_TYPES.retrieveHolder(new RegistryKey(NAMESPACE, name));
    }

    private static RegistryHolder<DimensionType> ofMinecraft(@NotNull String name) {
        return CardinalRegistries.DIMENSION_TYPES.retrieveHolder(new RegistryKey(Key.MINECRAFT_NAMESPACE, name));
    }

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
