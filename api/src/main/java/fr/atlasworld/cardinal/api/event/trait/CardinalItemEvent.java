package fr.atlasworld.cardinal.api.event.trait;

import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;

import java.util.Optional;

/**
 * Represents any event called about an {@link CardinalItem}.
 */
public interface CardinalItemEvent extends ItemEvent {

    /**
     * Retrieve the {@link CardinalItem} related to the event.
     *
     * @return retrieve the item related to the event, if the {@link ItemStack#isAir()} this will return {@code null}.
     */
    default Optional<CardinalItem> getItem() {
        return CardinalItem.fromStack(this.getItemStack());
    }
}
