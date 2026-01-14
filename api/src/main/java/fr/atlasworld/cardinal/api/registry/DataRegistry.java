package fr.atlasworld.cardinal.api.registry;

import com.google.common.collect.BiMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Specialized {@link Registry} used for data-driven entries, which can be cleared then repopulated when Cardinal is reloading.
 * <br><br>
 * The registry is intended for values being stored inside are data-driven if you want to store more traditional entries
 * that shouldn't be cleared or reloaded prefer using {@link SimpleRegistry}.
 * <br><br>
 * Any reference from the registry should <b>NEVER</b> be kept and always be retrieved from the registry
 * to allow for updated values to seamlessly be applied to already running processes.
 *
 * @param <T> type of values contained in the registry.
 */
public class DataRegistry<T> extends SimpleRegistry<T> implements ReloadableRegistry<T> {
    private final ReentrantReadWriteLock reloadLock = new ReentrantReadWriteLock();

    /**
     * Create a new data registry.
     *
     * @param key key of the registry.
     */
    public DataRegistry(@NotNull Key key) {
        super(key);
    }

    /**
     * Create a new data registry.
     *
     * @param key key of the registry.
     * @param map underlying map that will be used in the registry.
     *
     * @deprecated Passing an already existing {@link BiMap} is unsafe, as it can be modified externally.
     *             Use {@link #DataRegistry(Key)} instead.
     */
    @Deprecated
    public DataRegistry(@NotNull Key key, @NotNull BiMap<Key, T> map) {
        super(key, map);
    }

    @Override
    public void freezeRegistry() {
        if (this.frozen.get())
            throw new IllegalStateException("Registry has already been finalized.");

        if (this.reloadLock.isWriteLocked())
            this.reloadLock.writeLock().unlock();

        this.frozen.set(true);
    }

    @Override
    public boolean containsKey(@NotNull Key key) {
        this.reloadLock.readLock().lock();
        try {
            return super.containsKey(key);
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public boolean containsValue(@NotNull T value) {
        this.reloadLock.readLock().lock();
        try {
            return super.containsValue(value);
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        this.reloadLock.readLock().lock();
        try {
            return super.isEmpty();
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public Optional<T> retrieveValue(@NotNull Key key) {
        this.reloadLock.readLock().lock();
        try {
            return super.retrieveValue(key);
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public Optional<Key> retrieveKey(@NotNull T value) {
        this.reloadLock.readLock().lock();
        try {
            return super.retrieveKey(value);
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<T> values() {
        this.reloadLock.readLock().lock();
        try {
            return super.values();
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public @NotNull Set<Key> keys() {
        this.reloadLock.readLock().lock();
        try {
            return super.keys();
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    @Override
    public Set<Map.Entry<Key, T>> entries() {
        this.reloadLock.readLock().lock();
        try {
            return super.entries();
        } finally {
            this.reloadLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @ApiStatus.Internal
    public void reload() {
        this.reloadLock.writeLock().lock(); // Prevent any threads from reading the registry during reload.
        try {
            this.entries.clear();
            this.frozen.set(false);
        } catch (Throwable e) {
            this.reloadLock.readLock().unlock();
        }
    }
}
