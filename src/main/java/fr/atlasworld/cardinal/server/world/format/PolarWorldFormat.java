package fr.atlasworld.cardinal.server.world.format;

import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import net.hollowcube.polar.PolarLoader;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class PolarWorldFormat implements GameWorldFormat {

    @Override
    public @NotNull String extension() {
        return "polar";
    }

    @Override
    public @NotNull IChunkLoader load(@NotNull InputStream stream) throws IOException {
        return new PolarLoader(stream).setParallel(true);
    }

    @Override
    public byte[] save(@NotNull IChunkLoader loader) throws IOException {
        return PolarWriter.write(((PolarLoader) loader).world());
    }

    @Override
    public @NotNull IChunkLoader supplyBlankLoader(@NotNull DimensionType dimension) {
        return new PolarLoader(new PolarWorld(dimension));
    }
}
