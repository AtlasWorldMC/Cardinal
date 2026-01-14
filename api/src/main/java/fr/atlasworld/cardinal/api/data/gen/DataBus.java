package fr.atlasworld.cardinal.api.data.gen;

import com.google.gson.JsonElement;
import fr.atlasworld.cardinal.api.data.gen.provider.DataProvider;
import fr.atlasworld.cardinal.api.util.KeyableNamespaced;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Data bus, main entry for data generation.
 */
public interface DataBus extends KeyableNamespaced {

    /**
     * Retrieve the namespace of the current data bus.
     * <br>
     * The namespace is associated with the plugin from which the {@link DataGenerator} is loaded from.
     *
     * @return namespace of the current data bus.
     */
    @NotNull @KeyPattern.Namespace String namespace();

    /**
     * Data generator logger.
     *
     * @return generator logger.
     */
    @NotNull Logger logger();

    /**
     * Register a data provider.
     *
     * @param provider data provider to register.
     */
    void registerProvider(@NotNull DataProvider provider);

    /**
     * Register multiple data providers.
     *
     * @param providers data providers to register.
     */
    void registerProviders(@NotNull DataProvider... providers);

    /**
     * Write a generated file to the data bus.
     *
     * @param provider data provider that generated the file.
     * @param key key to of the file to write.
     * @param json JSON element to write.
     *
     * @throws IOException if the file could not be written.
     */
    void write(@NotNull DataProvider provider, @NotNull Key key, @NotNull JsonElement json) throws IOException;

    /**
     * Open an output stream to write to the data bus.
     *
     * @param provider data provider that generated the file.
     * @param key key to of the file to write.
     *
     * @return output stream to write to.
     * @throws IOException if the stream could not be opened.
     */
    @NotNull OutputStream openStream(@NotNull DataProvider provider, @NotNull Key key) throws IOException;
}
