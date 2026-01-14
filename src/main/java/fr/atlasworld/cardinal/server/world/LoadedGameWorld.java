package fr.atlasworld.cardinal.server.world;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.InstanceContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;

public final class LoadedGameWorld implements GameWorld {
    private final ResourceSource source;
    private final Meta meta;

    public LoadedGameWorld(@NotNull Meta meta, ResourceSource source) throws IOException {
        this.source = source;
        this.meta = meta;
    }

    @Override
    public int revision() {
        return this.meta.revision();
    }

    @Override
    public @NotNull Set<Component> authors() {
        return this.meta.authors();
    }

    @Override
    public boolean generated() {
        return false;
    }

    @Override
    public void provide(@NotNull InstanceContainer instance, @Nullable CompoundBinaryTag params) throws IOException {
        Preconditions.checkNotNull(instance, "Instance cannot be null!");

        try {
            IChunkLoader loader = this.meta.format.get().load(this.source.openStream());
            instance.setChunkLoader(loader);
        } catch (Throwable ex) {
            throw new IOException("World loading failed: ", ex);
        }
    }

    public record Meta(RegistryHolder<GameWorldFormat> format, int revision, Set<Component> authors) {
        public static final Codec<Meta> CODEC = StructCodec.struct(
                "format", Codec.KEY, meta -> meta.format().key(),
                "rev", Codec.INT.optional(0), Meta::revision,
                "authors", Serializers.MINI_MESSAGE_CODEC.set(), Meta::authors,
                Meta::fromCodec
        );

        private static Meta fromCodec(@NotNull Key formatKey, int revision, @NotNull Set<Component> authors) {
            Preconditions.checkNotNull(formatKey, "Format key cannot be null!");
            Preconditions.checkNotNull(authors, "Authors cannot be null!");

            RegistryHolder<GameWorldFormat> format = CardinalRegistries.WORLD_FORMATS.retrieveHolder(formatKey);
            Preconditions.checkArgument(format.referencePresent(), "No world format with key '" + formatKey + "' exist!");

            return new Meta(format, revision, authors);
        }
    }
}
