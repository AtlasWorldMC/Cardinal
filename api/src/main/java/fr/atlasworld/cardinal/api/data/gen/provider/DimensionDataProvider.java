package fr.atlasworld.cardinal.api.data.gen.provider;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.minestom.server.codec.Codec;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * {@link DimensionType} data provider, used to define dimensions through data packs.
 */
public abstract class DimensionDataProvider extends CodecDataProvider<DimensionType> {

    @Override
    public @NotNull Type type() {
        return Type.DATA;
    }

    public @NotNull String path() {
        return "dimension_type";
    }

    @Override
    public final @NotNull Codec<DimensionType> codec() {
        return DimensionType.REGISTRY_CODEC;
    }

    /**
     * Create a dimension type.
     *
     * @param bus data bus.
     * @param dim registry holder for which the dimension is created.
     * @param builder builder setting the settings of the dimension.
     *
     * @throws IOException if the dimension could not be written to the bus.
     */
    public void createDim(@NotNull DataBus bus, @NotNull RegistryHolder<DimensionType> dim, @NotNull Consumer<DimensionType.Builder> builder) throws IOException {
        Preconditions.checkNotNull(bus, "Data bus cannot be null!");
        Preconditions.checkNotNull(dim, "Dimension type cannot be null!");
        Preconditions.checkNotNull(builder, "Builder cannot be null!");

        DimensionType.Builder dimBuilder = DimensionType.builder();
        builder.accept(dimBuilder);
        this.write(bus, dim.key(), dimBuilder.build());
    }
}
