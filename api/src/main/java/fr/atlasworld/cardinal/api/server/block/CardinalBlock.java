package fr.atlasworld.cardinal.api.server.block;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.server.item.CardinalBlockItem;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Represents a custom item on cardinal.
 */
public class CardinalBlock {
    protected final Material base;
    protected final boolean tickable;

    protected @Nullable CardinalBlockItem item;

    /**
     * Retrieve the CardinalBlock associated with a {@link Block}.
     *
     * @param block block to retrieve the CardinalBlock from.
     * @return optional containing the CardinalBlock or an empty optional if the block is not a CardinalBlock.
     */
    public static Optional<CardinalBlock> fromBlock(@NotNull Block block) {
        return DelegateFactory.blockDelegate().getCardinalBlock(block);
    }

    /**
     * Create a new CardinalBlock.
     *
     * @param properties block properties.
     */
    public CardinalBlock(@NotNull BlockProperties properties) {
        Preconditions.checkNotNull(properties, "Block properties cannot be null!");

        this.base = properties.base;
        this.tickable = properties.tickable;
    }

    /**
     * Sets the {@link CardinalBlockItem} associated with this block.
     *
     * @param item item.
     */
    public void setItem(@Nullable CardinalBlockItem item) {
        this.item = item;
    }

    /**
     * Retrieve the {@link CardinalBlockItem} associated with this block.
     *
     * @return {@link CardinalBlockItem} associated with this block, or {@code null} if none is set.
     */
    public @Nullable CardinalBlockItem getItem() {
        return this.item;
    }

    /**
     * Retrieve this {@link CardinalBlock} as a {@link Block}.
     *
     * @return block representation of this {@link CardinalBlock}.
     */
    public Block asBlock() {
        return DelegateFactory.blockDelegate().getBlock(this);
    }

    /**
     * Retrieve the material this block is based on.
     *
     * @return material of this block.
     */
    public final Material base() {
        return this.base;
    }

    /**
     * Whether the block is tickable or not.
     * <br>
     * You can set this parameter in the {@link BlockProperties#setTickable(boolean)} constructor.
     *
     * @return {@code true} if the block is tickable, {@code false} otherwise.
     */
    public final boolean tickable() {
        return this.tickable;
    }

    /**
     * Called every tick when a block needs to be updated.
     * <br>
     * This method is only called if the block is {@link #tickable() tickable}.
     * <br>
     * <b>Warning:</b> doing expensive operations in this method will slow down the server.
     *
     * @param tick ticking details.
     */
    public void tick(BlockHandler.Tick tick) {
    }

    /**
     * Called when the block has been placed.
     * <br>
     * When placed by a player you can cast {@code placement} to {@link BlockHandler.PlayerPlacement} to retrieve additional information.
     *
     * @param placement placement details.
     */
    public void onPlace(BlockHandler.Placement placement) {
    }

    /**
     * Called when the block has been destroyed or replaced.
     * When placed by a player you can cast {@code destroy} to {@link BlockHandler.PlayerDestroy} to retrieve additional information.
     *
     * @param destroy destruction details.
     */
    public void onDestroy(BlockHandler.Destroy destroy) {
    }

    /**
     * Called when a player interacts with this block.
     *
     * @param interaction interaction details.
     * @return whether to allow the block placement if the player has a block in its hand,
     *         this should be used, for example, when opening a container to prevent placing the block when clicking on it.
     *         {@code true} to allow the block placement, {@code false} to prevent it.
     */
    public boolean onInteract(BlockHandler.Interaction interaction) {
        return true;
    }

    /**
     * Called when an entity touches this block.
     *
     * @param touch touch details.
     */
    public void onTouch(BlockHandler.Touch touch) {
    }

    /**
     * Specifies which block entity tags should be sent to the player.
     *
     * @return The list of tags from this block's block entity that should be sent to the player
     * @see <a href="https://minecraft.wiki/w/Block_entity">Block entity on the Minecraft wiki</a>
     */
    public Collection<Tag<?>> blockEntityTags() {
        return List.of();
    }

    /*
     * TODO: Set a proper documentation, this is unclear atm what it does, but it seems to be used to represent some kind of state,
     *  weirdly enough even in the BlockHandler it seems not serve no purpose.
     */
    public byte currentBlockEntityAction() {
        return -1;
    }

    /**
     * Block properties.
     */
    public static class BlockProperties {
        protected final Material base;
        protected boolean tickable;

        /**
         * Create a new block property.
         *
         * @param base base material representing the underlying block.
         * @throws IllegalArgumentException if the material is air or not a block.
         */
        public BlockProperties(@NotNull Material base) {
            Preconditions.checkNotNull(base, "Base material cannot be null!");
            Preconditions.checkArgument(base != Material.AIR, "Base material cannot be air!");
            Preconditions.checkArgument(base.isBlock(), "Base material must be a block!");

            this.base = base;
            this.tickable = false;
        }

        /**
         * Create a new block property with a {@link Material#STONE stone} as base.
         */
        public BlockProperties() {
            this(Material.STONE);
        }

        /**
         * Whether the block should be updated at every tick or not.
         * <br>
         * <b>Warning:</b> doing expensive operations in this method will slow down the server.
         *
         * @param tickable {@code true} if the block should be updated, {@code false} otherwise.
         */
        public void setTickable(boolean tickable) {
            this.tickable = tickable;
        }
    }
}
