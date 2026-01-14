package fr.atlasworld.cardinal.delegate;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.delegate.ItemDelegate;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;

public final class ItemDelegateImpl implements ItemDelegate {
    private static final Logger LOGGER = Logging.logger();

    @Override
    public @NotNull ItemStack createStack(@NotNull CardinalItem item) {
        return this.createStack(item, 1);
    }

    @Override
    public @NotNull ItemStack createStack(@NotNull CardinalItem item, int amount) {
        Preconditions.checkNotNull(item, "Item cannot be null");
        Preconditions.checkArgument(amount > 0 && amount <= item.maxStackSize(), "Amount must be greater than 0 and less than the maxStackSize of the item");

        Optional<Key> key = CardinalRegistries.ITEMS.retrieveKey(item);
        if (key.isEmpty())
            throw new IllegalArgumentException("The item is not registered!");

        return ItemStack.of(item.base(), amount).with(builder -> {
            item.apply(key.get(), builder);
            builder.setTag(CardinalItem.CARDINAL_ITEM_TAG, key.get());
        });
    }

    @Override
    public Optional<CardinalItem> getItem(@NotNull ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");

        if (stack.isAir())
            return Optional.empty();

        Key key;
        if (stack.hasTag(CardinalItem.CARDINAL_ITEM_TAG))
            key = stack.getTag(CardinalItem.CARDINAL_ITEM_TAG);
        else
            key = stack.material().key();

        if (key == null) {
            LOGGER.error("Failed to read '{}' tag from stack, cannot determine item.", CardinalItem.CARDINAL_ITEM_TAG.getKey());
            return Optional.empty();
        }

        Optional<CardinalItem> item = CardinalRegistries.ITEMS.retrieveValue(key);
        if (item.isEmpty())
            LOGGER.error("Found stack with unknown item key: {}", key);

        return item;
    }
}
