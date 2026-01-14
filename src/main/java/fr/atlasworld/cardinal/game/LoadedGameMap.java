package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.util.Serializers;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LoadedGameMap(@NotNull Component name, @NotNull Component description, @NotNull RegistryHolder<Game> game, @NotNull List<MapWorld> worlds) implements GameMap {
    public static final Codec<LoadedGameMap> CODEC = StructCodec.struct(
            "name", Serializers.MINI_MESSAGE_CODEC, LoadedGameMap::name,
            "description", Serializers.MINI_MESSAGE_CODEC.optional(Component.empty()), LoadedGameMap::description,
            "game", Codec.KEY, map -> map.game().key(),
            "worlds", LoadedMapWorld.CODEC.list(), map -> map.worlds().stream().map(world -> (LoadedMapWorld) world).toList(),
            LoadedGameMap::fromCodec
    );

    public static LoadedGameMap fromCodec(@NotNull Component name, @NotNull Component description, @NotNull Key gameKey, @NotNull List<LoadedMapWorld> worlds) {
        Preconditions.checkNotNull(name, "Name cannot be null!");
        Preconditions.checkNotNull(description, "Description cannot be null!");
        Preconditions.checkNotNull(gameKey, "Game key cannot be null!");
        Preconditions.checkNotNull(worlds, "Worlds cannot be null!");

        RegistryHolder<Game> game = CardinalRegistries.GAMES.retrieveHolder(gameKey);
        Preconditions.checkArgument(game.referencePresent(), "No game with key '" + gameKey + "' exist!");

        return new LoadedGameMap(name, description, game, worlds.stream().map(world -> (MapWorld) world).toList());
    }

    public record LoadedMapWorld(@NotNull RegistryHolder<GameWorld> world, @NotNull String dimension, @NotNull CompoundBinaryTag extraParams) implements MapWorld {
        public static final Codec<LoadedMapWorld> CODEC = StructCodec.struct(
                "key", Codec.KEY, (world) -> world.world().key(),
                "dimension", Codec.STRING, LoadedMapWorld::dimension,
                "params", Codec.NBT_COMPOUND.optional(CompoundBinaryTag.empty()), LoadedMapWorld::extraParams,
                LoadedMapWorld::fromCodec
        );

        private static LoadedMapWorld fromCodec(@NotNull Key worldKey, @NotNull String dimension, @NotNull CompoundBinaryTag params) {
            Preconditions.checkNotNull(worldKey, "World key cannot be null!");
            Preconditions.checkNotNull(dimension, "Dimension key cannot be null!");
            Preconditions.checkNotNull(params, "Params cannot be null!");

            Preconditions.checkArgument(!dimension.isEmpty(), "Dimension key cannot be empty!");

            RegistryHolder<GameWorld> world = CardinalRegistries.WORLDS.retrieveHolder(worldKey);
            Preconditions.checkArgument(world.referencePresent(), "No world with '" + worldKey + "' exist!");
            return new LoadedMapWorld(world, dimension, params);
        }
    }
}
