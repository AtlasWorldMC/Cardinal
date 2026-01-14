package fr.atlasworld.cardinal.profiler.world;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.data.DatapackImpl;
import me.lucko.spark.common.platform.world.ChunkInfo;
import me.lucko.spark.common.platform.world.WorldInfoProvider;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class CardinalWorldInfoProvider implements WorldInfoProvider {
    private final Set<DataPackInfo> dataPacks;

    public CardinalWorldInfoProvider() {
        this.dataPacks = this.sparkDataPacks();
    }

    @Override
    public CountsResult pollCounts() {
        return new CountsResult(
                MinecraftServer.getConnectionManager().getOnlinePlayerCount(),
                this.countEntities(),
                0, // Minestom (and cardinal) does not have a TileEntity / BlockEntities
                this.countChunks()
        );
    }

    @Override
    public boolean mustCallSync() {
        return false;
    }

    private int countEntities() {
        int entities = 0;

        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            entities += instance.getEntities().size();
        }

        return entities;
    }

    private int countChunks() {
        int chunks = 0;

        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            chunks += instance.getChunks().size();
        }

        return chunks;
    }

    @Override
    public ChunksResult<? extends ChunkInfo<?>> pollChunks() {
        ChunksResult<CardinalChunkInfo> result = new ChunksResult<>();

        for (Instance instance : MinecraftServer.getInstanceManager().getInstances()) {
            result.put(instance.getUuid().toString(), instance.getChunks().stream().map(CardinalChunkInfo::new).toList());
        }

        return result;
    }

    @Override
    public GameRulesResult pollGameRules() {
        return new GameRulesResult(); // No game-rules exist.
    }

    @Override
    public Collection<DataPackInfo> pollDataPacks() {
        return this.dataPacks;
    }

    private @NotNull Set<WorldInfoProvider.DataPackInfo> sparkDataPacks() {
        return CardinalServer.getServer().dataManager().loadedDatapacks().stream()
                .map(pack -> new WorldInfoProvider.DataPackInfo(
                        Serializers.PLAIN_TEXT.serialize(pack.name()),
                        Serializers.PLAIN_TEXT.serialize(pack.description()),
                        determinePackSource(pack)
                ))
                .collect(Collectors.toUnmodifiableSet());
    }

    private String determinePackSource(Datapack pack) {
        return switch (pack) {
            case Plugin ignored -> "Plugin";
            case CardinalServer ignored -> "Builtin";
            case DatapackImpl ignored -> "Datapack";
            default -> "Unknown";
        };
    }
}
