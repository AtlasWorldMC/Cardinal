package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.CardinalDataComponents;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a block item. An item capable of placing a block.
 */
public class CardinalBlockItem extends CardinalItem {
    public static final String BLOCK_TRANSLATION_KEY = "block.%s.%s";

    private final RegistryHolder<CardinalBlock> block;

    public CardinalBlockItem(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom) {
        super(base, prototype, custom);

        Preconditions.checkArgument(base.isBlock(), "Base material must be a block!");

        Key blockKey = this.get(CardinalDataComponents.BLOCK_ITEM_BLOCK);
        Preconditions.checkNotNull(blockKey, "Missing '" + CardinalDataComponents.BLOCK_ITEM_BLOCK.key() + "' component in item prototype.");

        this.block = CardinalRegistries.BLOCKS.retrieveHolder(blockKey);
    }

    /**
     * Retrieve the block that this item places.
     *
     * @return block that this item places.
     */
    public @NotNull RegistryHolder<CardinalBlock> block() {
        return this.block;
    }

    @Override
    public @NotNull BlockInteractionResult onBlockPlace(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, @NotNull Block block, @NotNull Point blockPos, @NotNull BlockFace face) {
        return BlockInteractionResult.replace(this.block.get().asBlock());
    }
}
