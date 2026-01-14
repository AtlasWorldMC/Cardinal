package fr.atlasworld.cardinal.api.server.component;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTag;
import net.minestom.server.codec.Codec;
import net.minestom.server.codec.Result;
import net.minestom.server.codec.Transcoder;
import net.minestom.server.component.DataComponents;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Similar to minestom's {@link net.minestom.server.component.DataComponent}, but for server-side utilization.
 * <br><br>
 * <b>Warning:</b> even these components are sent to the client, internally, they're stored inside the {@link DataComponents#CUSTOM_DATA} components.
 * <br>
 * A client with a mod will be able to read these components, storing game-sensitive data.
 *
 * @param <T> type fo data contained in the component.
 */
public final class ServerDataComponent<T> implements Codec<T> {
    private final Codec<T> codec;

    /**
     * Create a new server data component.
     *
     * @param codec codec to use for serialization/deserialization.
     */
    public ServerDataComponent(@NotNull Codec<T> codec) {
        Preconditions.checkNotNull(codec, "Codec cannot be null!");
        this.codec = codec;
    }

    @Override
    public <D> @NotNull Result<T> decode(@NotNull Transcoder<@NotNull D> coder, D value) {
        Check.notNull(this.codec, "{0} cannot be deserialized from Codec", this);
        return this.codec.decode(coder, value);
    }

    @Override
    public <D> @NotNull Result<D> encode(@NotNull Transcoder<@NotNull D> coder, @Nullable T value) {
        Check.notNull(this.codec, "{0} cannot be deserialized from Codec", this);
        return this.codec.encode(coder, value);
    }

    /**
     * Checks whether this component is present within the given source.
     *
     * @param key key of the component.
     * @param source the {@link TagReadable} source, where the component is stored.
     *
     * @return {@code true} if the component is present, {@code false} otherwise.
     */
    public boolean hasComponent(Key key, TagReadable source) {
        return source.hasTag(Tag.NBT(key.toString()));
    }

    /**
     * Retrieve the component from the given source.
     *
     * @param key key of the component.
     * @param source the {@link TagReadable} source, where the component is stored.
     *
     * @return the parsed component, or {@code null} if the component is not present.
     */
    public @Nullable T fromTag(Key key, TagReadable source) {
        BinaryTag tag = source.getTag(Tag.NBT(key.toString()));
        if (tag == null)
            return null;

        return this.decode(Transcoder.NBT, tag).orElse(null);
    }

    /**
     * Helper interface for objects that hold server-side data components.
     */
    public interface Holder {

        /**
         * Checks whether the holder has a component.
         *
         * @param component registry holder containing the component to look for.
         *
         * @return {@code true} if the holder has the component, {@code false} otherwise.
         * @param <T> type of the component.
         */
        default <T> boolean has(RegistryHolder<ServerDataComponent<T>> component) {
            return get(component) != null;
        }

        /**
         * Retrieve the value of a component.
         *
         * @param component registry holder containing the component to retrieve.
         *
         * @return the value of the component, or {@code null} if the component is not present.
         * @param <T> type of the component.
         */
        <T> @Nullable T get(RegistryHolder<ServerDataComponent<T>> component);

        /**
         * Retrieve the value of a component or a default value if the component is not present.
         *
         * @param component registry holder containing the component to retrieve.
         * @param defaultValue fallback value if the component is not present.
         *
         * @return the value of the component, or the fallback value if the component is not present.
         * @param <T> type of the component.
         */
        default <T> T get(RegistryHolder<ServerDataComponent<T>> component, T defaultValue) {
            final T value = get(component);
            return value != null ? value : defaultValue;
        }
    }
}
