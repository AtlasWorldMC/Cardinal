package fr.atlasworld.cardinal.data.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.atlasworld.cardinal.api.data.IndexedDataType;
import fr.atlasworld.cardinal.api.exception.data.DataLoadingException;
import fr.atlasworld.cardinal.api.exception.data.DataSerializationException;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.util.Priority;
import fr.atlasworld.cardinal.server.world.LoadedGameWorld;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Map;

public class WorldDataType extends IndexedDataType<GameWorld> {
    public static final Codec<Map<Key, LoadedGameWorld.Meta>> INDEX_CODEC = Codec.KEY.mapValue(LoadedGameWorld.Meta.CODEC);

    @Override
    protected void collectEntries(@NotNull JsonElement index, @NotNull Holder<GameWorld> holder) throws DataSerializationException {
        JsonObject data = index.getAsJsonObject();
        for (var entry : data.entrySet()) {
            Key key = Codec.KEY.decode(Transcoder.JSON)
        }
    }

    @Override
    protected @NotNull GameWorld loadEntry(@NotNull Key key, @NotNull JsonElement indexMeta, @NotNull Context<GameWorld> ctx) throws DataLoadingException, DataSerializationException {
        return null;
    }

    @Override
    public @UnknownNullability String indexFile() {
        return "worlds.json";
    }

    @Override
    public @NotNull String type() {
        return "world";
    }

    @Override
    public @NotNull Registry<GameWorld> registry() {
        return CardinalRegistries.WORLDS;
    }

    @Override
    public int priority() {
        return Priority.HIGH.priority();
    }
}
