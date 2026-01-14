package fr.atlasworld.cardinal.api.server.world;

import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Specialized interface to load specific game world formats.
 */
public interface GameWorldFormat {

    /**
     * Retrieve the file extension of this format.
     *
     * @return file extension to the format.
     */
    @NotNull String extension();

    /**
     * Load the world from the provided input stream.
     *
     * @param stream input stream of the world file.
     * @return IChunkLoader instance.
     * @throws IOException if an I/O error occurs, or that the world format is incorrect or corrupted.
     */
    @NotNull
    IChunkLoader load(@NotNull InputStream stream) throws IOException;

    /**
     * Save the world to the specified.
     *
     * @param loader loader of the world to save.
     * @return bytes to save.
     * @throws IOException if an I/O error occurs.
     */
    byte[] save(@NotNull IChunkLoader loader) throws IOException;

    /**
     * Retrieve a blank chunk loader, used within the editor to save new worlds.
     * <br>
     * Dimension for which the loader is created, mainly used to know the maximum and minimum Y levels.
     *
     * @return a blank chunk loader, where data can be written to.
     */
    @NotNull IChunkLoader supplyBlankLoader(@NotNull DimensionType dimension);
}
