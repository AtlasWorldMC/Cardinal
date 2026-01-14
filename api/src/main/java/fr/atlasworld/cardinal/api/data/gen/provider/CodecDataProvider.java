package fr.atlasworld.cardinal.api.data.gen.provider;

import com.google.gson.JsonElement;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Transcoder;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Used to generate content based of {@link Codec codecs}, which are used to encode the data into json.
 *
 * @param <T> type of element this provider provides.
 */
public abstract class CodecDataProvider<T> implements DataProvider {

    /**
     * Retrieve the codec used to encode the data.
     *
     * @return codec used to encode the data.
     */
    public abstract @NotNull Codec<T> codec();

    /**
     * Writes the element to the bus.
     *
     * @param bus the bus to write to.
     * @param key key of the element.
     * @param value actual element.
     *
     * @throws IllegalArgumentException if the provided value could not be encoded.
     * @throws IOException if the file could not be written.
     */
    protected void write(@NotNull DataBus bus, @NotNull Key key, @NotNull T value) throws IOException {
        JsonElement val = this.codec().encode(Transcoder.JSON, value)
                .orElseThrow("Failed to generate file for type '" + this.type() + "' inside '" + this.path() + ": " + key + "'");
        bus.write(this, key, val);
    }
}
