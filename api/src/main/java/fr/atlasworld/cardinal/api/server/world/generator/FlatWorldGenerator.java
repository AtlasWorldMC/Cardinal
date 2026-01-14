package fr.atlasworld.cardinal.api.server.world.generator;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.server.world.GeneratedGameWorld;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

// TODO: Allow the usage of custom blocks for flat worlds.
public final class FlatWorldGenerator extends GeneratedGameWorld {
    public static final int REVISION = 0;

    public FlatWorldGenerator() {
    }

    @Override
    public int revision() {
        return REVISION;
    }

    @Override
    public @NotNull Set<Component> authors() {
        return CardinalServer.getServer().authors();
    }

    @Override
    protected @NotNull Generator provideGenerator(@Nullable CompoundBinaryTag params) {
        return unit -> {
            Block layerBlock = parseBlock(params);
            int height = parseHeight(params);

            Point start = unit.absoluteStart();
            Point size = unit.size();

            for (int x = 0; x < size.blockX(); x++) {
                for (int z = 0; z < size.blockZ(); z++) {
                    for (int y = 0; y < Math.min(height - start.blockY(), size.blockY()); y++) {
                        unit.modifier().setBlock(start.add(x, y, z), layerBlock);
                    }
                }
            }
        };
    }

    private Block parseBlock(@Nullable CompoundBinaryTag params) {
        if (params == null || params.isEmpty())
            return Block.STONE;

        String material = params.getString("material");

        if (material.isEmpty())
            return Block.STONE;

        try {
            Block block = Block.fromKey(Key.key(material));
            if (block == null)
                return Block.STONE;

            return block;
        } catch (Throwable ex) {
            return Block.STONE;
        }
    }

    private int parseHeight(@Nullable CompoundBinaryTag params) {
        if (params == null || params.isEmpty())
            return 4;

        int height = params.getInt("height");
        if (height < 1)
            return 4;

        return height;
    }
}
