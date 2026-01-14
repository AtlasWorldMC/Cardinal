package fr.atlasworld.cardinal.api.registry;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.google.errorprone.annotations.ThreadSafe;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple implementation of a registry.
 *
 * @param <T> type of values contained in the registry.
 */
@ThreadSafe
public class SimpleRegistry<T> implements Registry<T> {
    protected final Key key;
    protected final BiMap<@NotNull Key, @NotNull T> entries;

    protected final AtomicBoolean frozen;
    protected final ReentrantReadWriteLock lock;

    /**
     * Constructs an instance of the {@code SimpleRegistry} with a specified {@link Key}.
     *
     * @param key the {@link Key} associated with this registry; must not be null.
     * @throws NullPointerException if {@code key} is null.
     */
    public SimpleRegistry(@NotNull Key key) {
        Preconditions.checkNotNull(key);

        this.key = key;

        this.entries = HashBiMap.create();
        this.frozen = new AtomicBoolean(false);
        this.lock = new ReentrantReadWriteLock();
    }

    /**
     * Constructs an instance of the {@code SimpleRegistry} with a specified key, map of entries,
     * and a reloadability flag.
     *
     * @param key the {@link Key} associated with this registry; must not be null.
     * @param map the {@link BiMap} containing the registry entries; must not be null.
     *
     * @throws NullPointerException if {@code key} or {@code map} is null.
     * @deprecated Passing an already existing {@link BiMap} is unsafe, as it can be modified externally.
     *             Use {@link #SimpleRegistry(Key)} instead.
     */
    @Deprecated
    protected SimpleRegistry(@NotNull Key key, @NotNull BiMap<Key, T> map) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(map);

        this.key = key;

        this.entries = map;
        this.frozen = new AtomicBoolean(false);
        this.lock = new ReentrantReadWriteLock();
    }

    @Override
    public @NotNull Key registryKey() {
        return this.key;
    }

    @Override
    public void register(@NotNull Key key, @NotNull T value) {
        if (this.frozen.get())
            throw new IllegalStateException("Cannot register entries when registry is finalized!");

        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(value);

        this.lock.writeLock().lock();
        try {
            Preconditions.checkArgument(!this.entries.containsKey(key), "An entry is already registered with this key: %s", key);
            Preconditions.checkArgument(!this.entries.containsValue(value), "This value has already been registered.");

            this.entries.put(key, value);
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void freezeRegistry() {
        if (this.frozen.get())
            throw new IllegalStateException("Registry has already been finalized.");

        this.frozen.set(true);
    }

    @Override
    public final boolean frozen() {
        return this.frozen.get();
    }

    @Override
    public boolean containsKey(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null!");

        return this.executeRead(() -> this.entries.containsKey(key));
    }

    @Override
    public boolean containsValue(@NotNull T value) {
        Preconditions.checkNotNull(key, "Value cannot be null!");

        return this.executeRead(() -> this.entries.containsValue(value));
    }

    @Override
    public boolean isEmpty() {
        return this.executeRead(this.entries::isEmpty);
    }

    @Override
    public Optional<T> retrieveValue(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null!");

        return this.executeRead(() -> Optional.ofNullable(this.entries.get(key)));
    }

    @Override
    public Optional<Key> retrieveKey(@NotNull T value) {
        Preconditions.checkNotNull(key, "Value cannot be null!");

        return this.executeRead(() -> Optional.ofNullable(this.entries.inverse().get(value)));
    }

    @Override
    public @NotNull RegistryHolder<T> retrieveHolder(@NotNull Key key) {
        return new RegistryHolder<>(key, this, null);
    }

    @Override
    public @NotNull Set<T> values() {
        return this.executeRead(this.entries::values);
    }

    @Override
    public @NotNull Set<Key> keys() {
        return this.executeRead(this.entries::keySet);
    }

    @Override
    public Set<Map.Entry<Key, T>> entries() {
        return this.executeRead(this.entries::entrySet);
    }

    protected final <E> E executeRead(Supplier<E> supplier) {
        this.lock.readLock().lock();
        try {
            return supplier.get();
        } finally {
            this.lock.readLock().unlock();
        }
    }
}
