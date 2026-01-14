package fr.atlasworld.cardinal.event.player;

import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.server.block.CardinalBlockHandler;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerPickBlockEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import org.slf4j.Logger;

import java.util.Optional;

public final class PlayerBlockHandler {
    private static final Logger LOGGER = Logging.logger();

    public static void onBlockBreak(PlayerBlockBreakEvent event) {
        ItemStack stack = event.getPlayer().getItemInMainHand();
        Optional<CardinalItem> item = CardinalItem.fromStack(stack);
        if (item.isEmpty())
            return;

        CardinalItem.BlockInteractionResult result = item.get().onBlockBreak(event.getPlayer(), stack, event.getBlock(), event.getBlockPosition(), event.getBlockFace());

        event.setCancelled(result.cancelled());
        if (!result.cancelled() && result.block() != null)
            event.setResultBlock(result.block());
    }

    public static void onBlockPlace(PlayerBlockPlaceEvent event) {
        ItemStack stack = event.getPlayer().getItemInHand(event.getHand());
        Optional<CardinalItem> item = CardinalItem.fromStack(stack);
        if (item.isEmpty())
            return;

        CardinalItem.BlockInteractionResult result = item.get().onBlockPlace(event.getPlayer(), event.getHand(), stack, event.getBlock(), event.getBlockPosition(), event.getBlockFace());

        event.setCancelled(result.cancelled());
        if (!result.cancelled() && result.block() != null)
            event.setBlock(result.block());
    }

    public static void onInteract(PlayerBlockInteractEvent event) {
        ItemStack stack = event.getPlayer().getItemInMainHand();
        Optional<CardinalItem> item = CardinalItem.fromStack(stack);
        if (item.isEmpty())
            return;

        CardinalItem.InteractionResult result = item.get().onBlockInteract(event.getPlayer(), event.getHand(), stack, event.getBlock(),
                event.getBlockPosition(), event.getCursorPosition(), event.getBlockFace());

        switch (result) {
            case BLOCK -> event.setBlockingItemUse(true);
            case CANCEL -> event.setCancelled(true);
            case IGNORE -> {
            }
        }
    }

    public static void onPickBlock(PlayerPickBlockEvent event) {
        Player player = event.getPlayer();
        Block targetBlock = event.getBlock();
        byte hotbarSlot = player.getHeldSlot();

        if (targetBlock.isAir())
            return;

        if (!(targetBlock.handler() instanceof CardinalBlockHandler handler)) {
            ItemStack stack = ItemStack.of(targetBlock.registry().material());
            player.getInventory().setItemStack(hotbarSlot, stack);
            return;
        }

        CardinalBlock block = handler.getBlock();
        CardinalItem item = block.getItem();
        if (item == null)
            return;

        try {
            ItemStack finalStack = item.onBlockPick(player, item.createStack(1), targetBlock, event.getBlockPosition(), event.isIncludeData());
            if (finalStack == null)
                return;

            player.getInventory().setItemStack(hotbarSlot, finalStack);
        } catch (Throwable e) {
            LOGGER.error("Failed to pass {} event to {}:", event.getClass().getSimpleName(), item.getClass().getSimpleName(), e);
        }
    }
}
