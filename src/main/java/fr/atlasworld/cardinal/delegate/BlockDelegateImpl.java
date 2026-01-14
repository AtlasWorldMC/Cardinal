package fr.atlasworld.cardinal.delegate;

import fr.atlasworld.cardinal.api.delegate.BlockDelegate;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.server.block.CardinalBlockHandler;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class BlockDelegateImpl implements BlockDelegate {
    @Override
    public @NotNull Block getBlock(@NotNull CardinalBlock block) {
        Optional<Block> actualBlock = CardinalRegistries.BLOCKS.getBlock(block);
        if (actualBlock.isEmpty())
            throw new IllegalArgumentException("CardinalBlock is not registered!");

        return actualBlock.get();
    }

    @Override
    public Optional<CardinalBlock> getCardinalBlock(@NotNull Block block) {
        BlockHandler handler = block.handler();
        if (!(handler instanceof CardinalBlockHandler cardinalHandler))
            return Optional.empty();

        return Optional.of(cardinalHandler.getBlock());
    }
}
