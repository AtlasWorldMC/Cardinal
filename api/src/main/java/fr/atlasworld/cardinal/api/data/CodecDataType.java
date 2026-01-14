package fr.atlasworld.cardinal.api.data;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import fr.atlasworld.cardinal.api.exception.data.DataException;
import fr.atlasworld.cardinal.api.exception.data.DataSerializationException;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.util.Priority;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

/**
 * Specialized data type that specifically used minestom's {@link Codec} system.
 * <p><b>Note:</b> this will only work for <b>JSON-based objects</b></p>
 *
 * @param <T> type of data this codec data type handles.
 */
public final class CodecDataType<T> implements DataType<T> {
    private final Codec<T> codec;
    private final Registry<T> registry;
    private final String type;
    private final int priority;

    /**
     * Creates a new codec data type.
     *
     * @param codec codec to use.
     * @param registry registry to use.
     * @param type string type, defines in which directory the data is loaded from,
     *             ex: {@code "world"} the data type would load entries in the {@code world/} directory.
     */
    public CodecDataType(@NotNull Codec<T> codec, @NotNull Registry<T> registry, @NotNull String type, int priority) {
        Preconditions.checkNotNull(codec, "Codec must not be null");
        Preconditions.checkNotNull(registry, "Registry must not be null");
        Preconditions.checkNotNull(type, "Type must not be null");

        this.codec = codec;
        this.registry = registry;
        this.type = type;
        this.priority = priority;
    }

    public CodecDataType(@NotNull Codec<T> codec, @NotNull Registry<T> registry, @NotNull String type) {
        this(codec, registry, type, Priority.NORMAL.priority());
    }

    /**
     * Creates a new codec data type, using the registry's key value as the type.
     *
     * @param codec codec to use.
     * @param registry registry to use.
     */
    public CodecDataType(@NotNull Codec<T> codec, @NotNull Registry<T> registry) {
        this(codec, registry, registry.registryKey().value());
    }

    /**
     * Retrieve the codec used by this data type.
     *
     * @return codec used by this data type.
     */
    public @NotNull Codec<T> codec() {
        return this.codec;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean indexed() {
        return false;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public @UnknownNullability String indexFile() {
        return null;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public @NotNull String type() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public @NotNull Registry<T> registry() {
        return this.registry;
    }

    /**
     * {@inheritDoc}
     *
     * @param entry {@inheritDoc}
     * @param key {@inheritDoc}
     * @param ctx {@inheritDoc}
     *
     * @throws DataException {@inheritDoc}
     */
    @Override
    public void load(@NotNull DataEntry entry, @NotNull Key key, @NotNull Context<T> ctx) throws DataException {
        final JsonElement data = entry.asSource().asJson();
        final Registry<T> registry = ctx.registry();

        Result<T> result = this.codec.decode(Transcoder.JSON, data);
        if (result instanceof Result.Error<T>(String message))
            throw new DataSerializationException("Failed to decode json object: " + message);

        registry.register(key, result.orElse(null));
    }
}
