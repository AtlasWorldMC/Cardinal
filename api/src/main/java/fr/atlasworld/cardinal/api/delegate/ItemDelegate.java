package fr.atlasworld.cardinal.api.delegate;

import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Internal delegate. Do not use this class!
 * Changes inside this class won't be documented.
 */
@ApiStatus.Internal
public interface ItemDelegate {
    @NotNull ItemStack createStack(@NotNull CardinalItem item);

    @NotNull ItemStack createStack(@NotNull CardinalItem item, int amount);

    Optional<CardinalItem> getItem(@NotNull ItemStack stack);
}
