package fr.atlasworld.cardinal.registry;

import fr.atlasworld.cardinal.api.event.registry.ItemRegistrationEvent;
import fr.atlasworld.cardinal.api.event.registry.RegistryRegistrationEvent;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameFightEngine;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.game.GameRule;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.server.*;
import fr.atlasworld.cardinal.api.server.component.ServerDataComponent;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.server.item.ItemProvider;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import fr.atlasworld.cardinal.delegate.RegistryDelegateImpl;
import fr.atlasworld.cardinal.server.item.StandardItems;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class CardinalRegistries {
    private static final Logger LOGGER = Logging.logger();
    public static final String NAMESPACE = "cardinal";

    public static final Registries REGISTRIES = new Registries(new RegistryKey(NAMESPACE, "registries"));

    // Private Registries - Those should not be used in the API.



    // Public registries

    public static final DataTypeRegistry DATA_TYPES = (DataTypeRegistry) fr.atlasworld.cardinal.api.registry.CardinalRegistries.DATA_TYPES;
    public static final Registry<GameWorldFormat> WORLD_FORMATS = fr.atlasworld.cardinal.api.registry.CardinalRegistries.WORLD_FORMAT;
    public static final Registry<GameWorld> WORLDS = fr.atlasworld.cardinal.api.registry.CardinalRegistries.WORLDS;
    public static final Registry<GameMap> MAPS = fr.atlasworld.cardinal.api.registry.CardinalRegistries.MAPS;
    public static final Registry<StructurePool> STRUCTURES = fr.atlasworld.cardinal.api.registry.CardinalRegistries.STRUCTURES;

    public static final MinestomBackedRegistry<DimensionType> DIMENSION_TYPES = (MinestomBackedRegistry<DimensionType>) fr.atlasworld.cardinal.api.registry.CardinalRegistries.DIMENSION_TYPES;
    public static final Registry<Game> GAMES = fr.atlasworld.cardinal.api.registry.CardinalRegistries.GAMES;
    public static final Registry<GameFightEngine> FIGHT_ENGINES = fr.atlasworld.cardinal.api.registry.CardinalRegistries.FIGHT_ENGINES;

    public static final Registry<ServerDataComponent<?>> DATA_COMPONENTS = fr.atlasworld.cardinal.api.registry.CardinalRegistries.DATA_COMPONENTS;
    public static final Registry<ItemProvider> ITEM_PROVIDERS = fr.atlasworld.cardinal.api.registry.CardinalRegistries.ITEM_PROVIDERS;

    public static final BlockRegistry BLOCKS = (BlockRegistry) fr.atlasworld.cardinal.api.registry.CardinalRegistries.BLOCKS;
    public static final Registry<CardinalItem> ITEMS = fr.atlasworld.cardinal.api.registry.CardinalRegistries.ITEMS;
    public static final EnchantmentRegistry ENCHANTMENTS = (EnchantmentRegistry) fr.atlasworld.cardinal.api.registry.CardinalRegistries.ENCHANTMENTS;

    public static final Registry<GameRule<?>> GAME_RULES = fr.atlasworld.cardinal.api.registry.CardinalRegistries.GAME_RULES;

    public static void initialize() {
        LOGGER.info("Registering server registries...");

        CardinalDataTypes.init();
        CardinalWorldFormats.init();
        CardinalDimensionTypes.init();

        CardinalGameWorlds.init();
        CardinalGames.init();
        CardinalFightEngines.init();

        CardinalDataComponents.init();
        CardinalItemProviders.init();

        CardinalGameRules.init();

        RegistryRegistrationEvent event = new RegistryRegistrationEvent(REGISTRIES);
        EventDispatcher.call(event);
        REGISTRIES.freezeRegistry(); // Prevent any further modification
    }

    public static void load(boolean reload) {
        if (reload) {
            LOGGER.warn("Reloading registries, this is not recommended and may cause issues.");
            REGISTRIES.reloadRegistries();
        } else {
            registerDefaults();
        }

        REGISTRIES.callRegistries();
    }

    public static void freezeRegistries() {
        REGISTRIES.freezeRegistries();
    }

    private static void registerDefaults() {
        registerItems();
    }

    private static void registerItems() {
        RegistryDelegateImpl.registerEventNode().addListener(ItemRegistrationEvent.class, event -> {
            StandardItems.register(CardinalRegistries.ITEMS);
        });
    }

    private static RegistryKey createCardinalKey(@NotNull String key) {
        return new  RegistryKey(NAMESPACE, key);
    }
}
