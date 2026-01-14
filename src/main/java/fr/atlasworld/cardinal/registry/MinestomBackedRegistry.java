package fr.atlasworld.cardinal.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Registry implementation that is backed by a Minestom {@link DynamicRegistry}.
 *
 * @param <T> type of values contained in the registry.
 */
// Minestom's registry is a hellscape, it's good when if you stay within the bounds of it, but try to go out and the worst nightmare you can imagine will open to you.
// DON'T EVER DREAM OF GETTING THEM TO WORK WITH RELOADABLE ELEMENTS
public final class MinestomBackedRegistry<T> implements Registry<T> {
    private final Key key;
    private final DynamicRegistry<@NotNull T> registry;

    // used to lock registry access during reload, read lock is used when accessing the registry, and the write lock locks the registry during reload.
    private final AtomicBoolean frozen;

    public MinestomBackedRegistry(@NotNull Key key, @NotNull DynamicRegistry<@NotNull T> registry) {
        this.key = key;
        this.registry = registry;

        this.frozen = new AtomicBoolean(false);
    }

    @Override
    public @NotNull Key registryKey() {
        return this.key;
    }

    @Override
    public void register(@NotNull Key key, @NotNull T value) {
        if (this.frozen.get())
            throw new IllegalStateException("Cannot register entries when registry is finalized!");

        Preconditions.checkArgument(!this.containsKey(key), "An entry is already registered with this key: %s", key);
        Preconditions.checkArgument(!this.containsValue(value), "An entry is already registered with this value: %s", value);

        this.registry.register(key, value);
    }

    @Override
    public void freezeRegistry() {
        if (this.frozen.get())
            throw new IllegalStateException("Registry has already been finalized.");

        this.frozen.set(true);
    }

    @Override
    public boolean frozen() {
        return this.frozen.get();
    }

    @Override
    public boolean containsKey(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return this.registry.getKey(key) != null;
    }

    @Override
    public boolean containsValue(@NotNull T value) {
        Preconditions.checkNotNull(value, "Value cannot be null");
        return this.registry.getKey(value) != null;
    }

    @Override
    public boolean isEmpty() {
        return this.registry.size() == 0;
    }

    @Override
    public Optional<T> retrieveValue(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return Optional.ofNullable(this.registry.get(key));
    }

    @Override
    public Optional<Key> retrieveKey(@NotNull T value) {
        Preconditions.checkNotNull(value, "Value cannot be null");
        return Optional.ofNullable(this.registry.getKey(value)).map(Keyed::key);
    }

    @Override
    public @NotNull RegistryHolder<T> retrieveHolder(@NotNull Key key) {
        return new RegistryHolder<>(key, this, null);
    }

    public Optional<net.minestom.server.registry.RegistryKey<@NotNull T>> getMinestomKey(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return Optional.ofNullable(this.registry.getKey(key));
    }

    @Override
    public @NotNull Set<T> values() {
        return Set.copyOf(this.registry.values());
    }

    @Override
    public @NotNull Set<Key> keys() {
        return this.registry.keys().stream()
                .map(Keyed::key)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<Map.Entry<Key, T>> entries() {
        return this.registry.keys().stream()
                .filter(key -> this.registry.get(key) != null)
                .map(key -> Map.entry(key.key(), this.registry.get(key)))
                .collect(Collectors.toUnmodifiableSet());
    }
}
