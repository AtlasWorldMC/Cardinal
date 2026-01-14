package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.DataTypeRegistrationEvent;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.server.item.ItemPrototype;
import fr.atlasworld.cardinal.api.server.item.ItemProvider;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents all the {@link DataTypeOld}s registered by cardinal.
 */
public final class CardinalDataTypes {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<DataTypeOld<?, ?>> DATA_TYPES = DELEGATE.createInternalRegister(DataTypeRegistrationEvent.class);

    public static final RegistryHolder<DataTypeOld<?, GameWorld>> WORLD = DATA_TYPES.register("world", DELEGATE::retrieveGameWorldType);
    public static final RegistryHolder<DataTypeOld<?, GameMap>> MAP = DATA_TYPES.register("map", DELEGATE::retrieveGameMapType);
    public static final RegistryHolder<DataTypeOld<?, StructurePool>> STRUCTURE = DATA_TYPES.register("structure", DELEGATE::retrieveStructureType);
    
    public static final RegistryHolder<DataTypeOld<ItemPrototype, CardinalItem>> ITEM = DATA_TYPES.register("item", () ->
            DataTypeOld.direct("item", ItemPrototype.CODEC, CardinalRegistries.ITEMS, proto -> {
                ItemProvider provider = proto.provider().get();
                return provider.build(proto);
            }));

    public static final RegistryHolder<DataTypeOld<DimensionType, DimensionType>> DIMENSION_TYPE = DATA_TYPES.register("dimension_type",
            () -> DataTypeOld.direct("dimension_type", DimensionType.REGISTRY_CODEC, CardinalRegistries.DIMENSION_TYPES));

    private CardinalDataTypes() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
