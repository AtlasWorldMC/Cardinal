package fr.atlasworld.cardinal.profiler.world;

import me.lucko.spark.common.platform.world.ChunkInfo;
import me.lucko.spark.common.platform.world.CountMap;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;

import java.util.HashMap;

public final class CardinalChunkInfo implements ChunkInfo<Entity> {
    private final int x, z;
    private final CountMap<Entity> entityCounts;

    public CardinalChunkInfo(Chunk chunk) {
        this.x = chunk.getChunkX();
        this.z = chunk.getChunkZ();

        this.entityCounts = new CountMap.Simple<>(new HashMap<>());
        for (Entity entity : chunk.getInstance().getEntities()) {
            if (chunk.equals(entity.getChunk()))
                this.entityCounts.increment(entity);
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public CountMap<Entity> getEntityCounts() {
        return this.entityCounts;
    }

    @Override
    public String entityTypeName(Entity entity) {
        return entity.getEntityType().name().toLowerCase();
    }
}
