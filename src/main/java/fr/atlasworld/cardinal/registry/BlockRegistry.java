package fr.atlasworld.cardinal.registry;

import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.server.block.CardinalBlockHandler;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BlockRegistry extends CardinalRegistry<CardinalBlock> {
    private final Map<CardinalBlock, Block> blocks;

    public BlockRegistry(@NotNull Key key) {
        super(key);

        this.blocks = new ConcurrentHashMap<>();
    }

    @Override
    public void register(@NotNull Key key, @NotNull CardinalBlock value) {
        super.register(key, value);

        CardinalBlockHandler handler = new CardinalBlockHandler(key, value);
        Block block = value.base().block().withHandler(handler);

        this.blocks.put(value, block);
        MinecraftServer.getBlockManager().registerHandler(key, () -> handler);
    }

    public Optional<Block> getBlock(@NotNull CardinalBlock block) {
        return Optional.ofNullable(this.blocks.get(block));
    }
}
