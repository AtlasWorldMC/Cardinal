package fr.atlasworld.cardinal.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.CardinalItemProviders;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Class handling the mapping of the vanilla item prototype list from Minestom's Data into items Cardinal can work with.
 * <br><br>
 * Default items without any special behavior should not be registered manually, these will automatically be associated with
 * {@link fr.atlasworld.cardinal.api.server.item.CardinalItem}
 */
public final class StandardItems {
    private static final List<ItemPrototypeMapper> MAPPERS = new ArrayList<>();

    static {
        // TODO: Maybe add blocks ? Need to check client may use a different way of handling those kind of items.
        MAPPERS.add(new ItemPrototypeMapper(CardinalItemProviders.FOOD, components -> components.has(DataComponents.FOOD) && components.has(DataComponents.CONSUMABLE)));
        MAPPERS.add(new ItemPrototypeMapper(CardinalItemProviders.SHIELD, components -> components.has(DataComponents.BLOCKS_ATTACKS)));
    }

    public static void register(Registry<CardinalItem> registry) {
        for (Material material : Material.values()) {
            if (material == Material.AIR)
                continue; // Air cannot be used as an item, used to represent null.

            DataComponentMap prototype = material.prototype();
            CardinalItem item = null;

            for (ItemPrototypeMapper mapper : MAPPERS) {
                if (!mapper.support(prototype))
                    continue;

                item = mapper.map(material);
                Preconditions.checkNotNull(item, "Mapper failed to produce an item for %s.", material);
                break;
            }

            if (item == null) // Fallback if no mapper is available for the item, used for basic items
                item = CardinalItemProviders.BASIC.get().build(material, prototype, false);

            registry.register(material.key(), item);
        }
    }
}
