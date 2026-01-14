package fr.atlasworld.cardinal.api.registry;

import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.*;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameFightEngine;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.game.GameRule;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.api.server.component.ServerDataComponent;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.server.item.ItemProvider;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import net.minestom.server.MinecraftServer;
import net.minestom.server.world.DimensionType;

/**
 * Global class that maintains all Cardinal's core registries.
 */
public final class CardinalRegistries {
    public static final String NAMESPACE = "cardinal";
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();

    private CardinalRegistries() {
        throw new UnsupportedOperationException();
    }

    /*
     * ORDER MATTERS!!!!
     */

    // Data-related registries
    public static final Registry<DataTypeOld<?, ?>> DATA_TYPES = DELEGATE.dataTypeRegistry("data_types");
    public static final Registry<GameWorldFormat> WORLD_FORMAT = DELEGATE.cardinalRegistry("world_formats", WorldFormatRegistrationEvent::new);
    public static final Registry<GameWorld> WORLDS = DELEGATE.dataRegistry("worlds", WorldRegistrationEvent::new);
    public static final Registry<GameMap> MAPS = DELEGATE.dataRegistry("maps", null);
    public static final Registry<StructurePool> STRUCTURES = DELEGATE.dataRegistry("structures", null);

    public static final Registry<DimensionType> DIMENSION_TYPES = DELEGATE.minestomRegistry("dimension_types", MinecraftServer.getDimensionTypeRegistry(), null);
    public static final Registry<Game> GAMES = DELEGATE.cardinalRegistry("games", GameRegistrationEvent::new);
    public static final Registry<GameFightEngine> FIGHT_ENGINES = DELEGATE.cardinalRegistry("fight_engines", FightEngineRegistrationEvent::new);

    // Content
    public static final Registry<ServerDataComponent<?>> DATA_COMPONENTS = DELEGATE.cardinalRegistry("data_components", DataComponentRegistrationEvent::new);
    public static final Registry<ItemProvider> ITEM_PROVIDERS = DELEGATE.cardinalRegistry("item_providers", ItemProviderRegistrationEvent::new);

    public static final Registry<CardinalBlock> BLOCKS = DELEGATE.blockRegistry("blocks");
    public static final Registry<CardinalItem> ITEMS = DELEGATE.dataRegistry("items", ItemRegistrationEvent::new);
    public static final Registry<CardinalEnchantment> ENCHANTMENTS = DELEGATE.enchantmentRegistry("enchantments");

    // Should be dead last, as it may depend on other registries.
    public static final Registry<GameRule<?>> GAME_RULES = DELEGATE.cardinalRegistry("game_rules", GameRuleRegistrationEvent::new);
}
