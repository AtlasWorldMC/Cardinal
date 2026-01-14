package fr.atlasworld.cardinal.data.type;

import fr.atlasworld.cardinal.api.data.DataTypeOld;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import fr.atlasworld.cardinal.api.util.Priority;
import fr.atlasworld.cardinal.server.world.LoadedGameWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

// This is a way more complex data type, needs a proper dedicated class to handle.
public class WorldDataTypeOld implements DataTypeOld.TypeTransformer<Map<Key, LoadedGameWorld.Meta>, GameWorld> {
    public static final String WORLD_DIR = "world";
    public static final Codec<Map<Key, LoadedGameWorld.Meta>> INDEX_CODEC = Codec.KEY.mapValue(LoadedGameWorld.Meta.CODEC);

    public static final DataTypeOld<Map<Key, LoadedGameWorld.Meta>, GameWorld> INSTANCE = DataTypeOld.indirect("world", "worlds.json",
            INDEX_CODEC, CardinalRegistries.WORLDS, new WorldDataTypeOld(), Priority.HIGH.priority());

    @Override
    public void transform(@NotNull Map<Key, LoadedGameWorld.Meta> element, DataTypeOld.@NotNull TransformContext ctx, @NotNull Registry<GameWorld> registry) throws IOException {
        for (var entry : element.entrySet()) {
            Key key = entry.getKey();
            LoadedGameWorld.Meta meta = entry.getValue();

            GameWorldFormat format = meta.format().get();
            Optional<ResourceSource> source = ctx.createSource(key, WORLD_DIR, format.extension());
            if (source.isEmpty()) {
                ctx.logger().error("Could not find world source for '{}' located at: {}", key, key.namespace() + "/" + WORLD_DIR + "/" + key.value() + "." + format.extension());
                continue; // Continue to load other entries.
            }

            LoadedGameWorld world = new LoadedGameWorld(meta, source.get());
            registry.register(key, world);
        }
    }
}
