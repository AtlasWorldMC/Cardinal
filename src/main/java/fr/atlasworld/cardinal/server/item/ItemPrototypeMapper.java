package fr.atlasworld.cardinal.server.item;

import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.server.item.ItemProvider;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.item.Material;

import java.util.function.Predicate;

/**
 * Used to map vanilla materials to {@link CardinalItem}s.
 *
 * @param provider item provider to use.
 * @param predicate predicate to use to check if the item can be mapped by this mapper.
 */
public record ItemPrototypeMapper(RegistryHolder<ItemProvider> provider, Predicate<DataComponentMap> predicate) {
    public boolean support(DataComponentMap prototype) {
        return this.predicate.test(prototype);
    }

    public CardinalItem map(Material material) {
        return this.provider.get().build(material, material.prototype(), false);
    }
}
