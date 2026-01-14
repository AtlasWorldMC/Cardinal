package fr.atlasworld.cardinal.api.data;

import com.google.gson.JsonElement;
import fr.atlasworld.cardinal.api.exception.data.DataException;
import fr.atlasworld.cardinal.api.exception.data.DataLoadingException;
import fr.atlasworld.cardinal.api.exception.data.DataSerializationException;
import fr.atlasworld.cardinal.api.util.ThrowableRunnable;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a data type which is indexed, using a file to show where every entry is located,
 * and open them manually, with optionally additional meta-data attached to it.
 *
 * @param <T> type of data contained in the data type.
 */
public abstract class IndexedDataType<T> implements DataType<T> {
    protected final Logger logger;

    public IndexedDataType() {
        this.logger = LoggerFactory.getLogger("DataType-" + this.type());
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public final boolean indexed() {
        return true;
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
    public final void load(@NotNull DataEntry entry, @NotNull Key key, @NotNull Context<T> ctx) throws DataException {
        final JsonElement index = entry.asSource().asJson();
        final Holder<T> holder = new Holder<>();

        this.handleImplSafety(() -> this.collectEntries(index, holder));
        holder.load(ctx, this);
    }

    /**
     * Called when the index file is loaded and entries need to be collected before loading.
     *
     * @param index parsed json index file.
     * @param holder holder where entries should be collected.
     *
     * @throws DataSerializationException if the index could not be parsed properly.
     */
    protected abstract void collectEntries(@NotNull JsonElement index, @NotNull Holder<T> holder) throws DataSerializationException;

    /**
     * Called after entries have been collected, and now called for loading and deserialization.
     *
     * @param key key of the entry.
     * @param indexMeta data read from the index file attached to the key, collected during the {@link #collectEntries(JsonElement, Holder)} phase.
     * @param ctx loading context can be used to open new entries.
     */
    @NotNull
    protected abstract T loadEntry(@NotNull Key key, @NotNull JsonElement indexMeta, @NotNull Context<T> ctx) throws DataLoadingException, DataSerializationException;

    // Used internally to handle user implementation unexpected exceptions.
    @ApiStatus.Internal
    private void handleImplSafety(ThrowableRunnable task) throws DataException {
        try {
            task.run();
        } catch (DataException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new DataSerializationException(ex);
        }
    }

    /**
     * Simple data holder that wraps a hashmap to store collected entries and load them in one batch.
     *
     * @param <T> type of data contained in the holder.
     */
    public static final class Holder<T> {
        private final Map<Key, JsonElement> collectedEntries;

        public Holder() {
            this.collectedEntries = new HashMap<>();
        }

        /**
         * Collect an entry.
         *
         * @param key key of the entry.
         * @param element element to save.
         */
        public void collect(@NotNull Key key, @NotNull JsonElement element) {
            this.collectedEntries.put(key, element);
        }

        @ApiStatus.Internal
        private void load(Context<T> ctx, IndexedDataType<T> type) {
            for (var entry : this.collectedEntries.entrySet()) {
                try {
                    type.handleImplSafety(() -> {
                        T data = type.loadEntry(entry.getKey(), entry.getValue(), ctx);
                        ctx.registry().register(entry.getKey(), data);
                    });
                } catch (DataException ex) {
                    type.logger.error("Failed to load entry '{}': {}", entry.getKey(), ex.getMessage());
                }
            }
        }
    }
}
