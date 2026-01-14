package fr.atlasworld.cardinal.data.type;

import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.server.world.StructurePoolImpl;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class StructureDataType implements DataTypeOld.TypeTransformer<Map<Key, List<StructureDataType.StructureEntry>>, StructurePool> {
    private static final Codec<Map<Key, List<StructureEntry>>> CODEC = Codec.KEY.mapValue(StructureEntry.CODEC.list());
    private static final String STRUCTURE_DIR = "structure";

    public static final DataTypeOld<Map<Key, List<StructureEntry>>, StructurePool> INSTANCE = DataTypeOld.indirect(
            STRUCTURE_DIR, "structures.json", CODEC, CardinalRegistries.STRUCTURES, new StructureDataType());

    @Override
    public void transform(@NotNull Map<Key, List<StructureEntry>> element, DataTypeOld.@NotNull TransformContext ctx, @NotNull Registry<StructurePool> registry) throws IOException {
        for (var entry : element.entrySet()) {
            Key key = entry.getKey();
            List<StructureEntry> entries = entry.getValue();

            if (entries.isEmpty()) {
                ctx.logger().warn("Structure pool '{}' has no entries!", key);
                registry.register(key, new StructurePoolImpl(Set.of()));
                continue;
            }

            Set<StructurePoolImpl.StructurePoolEntry> poolEntries = new HashSet<>(entries.size());
            boolean brokenEntry = false;

            for (StructureEntry structureEntry : entries) {
                Key structureKey = structureEntry.key();
                Optional<ResourceSource> source = ctx.createSource(structureKey, STRUCTURE_DIR, "schem");

                if (source.isEmpty()) {
                    ctx.logger().error("Failed to construct structure pool '{}': Could not find structure source for '{}' located at: {}", key, structureKey, structureKey.namespace() + "/" + STRUCTURE_DIR + "/" + structureKey.value() + ".schem");
                    brokenEntry = true;
                    break;
                }

                poolEntries.add(structureEntry.toPoolEntry(source.get()));
            }

            if (!brokenEntry)
                registry.register(key, new StructurePoolImpl(poolEntries));
        }
    }

    public record StructureEntry(Key key, int weight) {
        private static final Codec<StructureEntry> CODEC = StructCodec.struct(
                "key", Codec.KEY, StructureEntry::key,
                "weight", Codec.INT.optional(1), StructureEntry::weight,
                StructureEntry::new
        );

        public StructurePoolImpl.StructurePoolEntry toPoolEntry(@NotNull ResourceSource source) {
            return new StructurePoolImpl.StructurePoolEntry(this.key, this.weight, source);
        }
    }
}
