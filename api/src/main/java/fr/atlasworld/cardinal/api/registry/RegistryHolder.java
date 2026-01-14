package fr.atlasworld.cardinal.api.registry;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.RestrictedApi;
import com.google.errorprone.annotations.ThreadSafe;
import fr.atlasworld.cardinal.api.annotation.DataGenProvider;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Holds the reference to a registry object contained in a {@link Registry}.
 *
 * @param <T> type of values held by this holder.
 */
@ThreadSafe
public final class RegistryHolder<T> {
    private final Key key;
    private final @Nullable Supplier<T> supplier;

    private @Nullable Registry<? super T> registry;

    public RegistryHolder(@NotNull Key key, @Nullable Registry<T> registry,  @Nullable Supplier<T> supplier) {
        Preconditions.checkNotNull(key);

        this.key = key;
        this.registry = registry;
        this.supplier = supplier;
    }

    public RegistryHolder(@NotNull Key key, @NotNull Supplier<T> supplier) {
        this(key, null, supplier);
    }

    /**
     * Verify if the reference is present.
     *
     * @return {@code true} if the reference is present, {@code false} otherwise.
     */
    public boolean referencePresent() {
        return this.registry != null && this.registry.retrieveValue(this.key).isPresent();
    }

    /**
     * Sets the registry the holder will lookup.
     * <br><br>
     * Internal use only, this should only be set by a {@link Register}.
     * <br>
     * {@link Registry Registries} creating holders should directly pass themselves the the holder through the constructor.
     *
     * @param registry registry to set.
     */
    @ApiStatus.Internal
    public synchronized void updateRegistry(@NotNull Registry<? super T> registry) {
        Preconditions.checkNotNull(registry);
        Preconditions.checkState(this.registry == null, "Registry has already been set.");

        this.registry = registry;
    }

    /**
     * Retrieve the reference of the object.
     *
     * @return reference of the object.
     * @throws NoSuchElementException if this registry object does not contain the reference to the object.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public T get() {
        if (this.registry == null)
            throw new NoSuchElementException("No registry set for value '" + this.key + "'!");

        Optional<T> value = (Optional<T>) this.registry.retrieveValue(this.key);
        if (value.isEmpty())
            throw new NoSuchElementException("No value present in registry '" + this.registry.registryKey() + "' for value '" + this.key + "' !");

        return value.get();
    }

    /**
     * This will attempt to retrieve the value of forcefully resolve the value.
     *
     * @return the value contained, or a newly created value, or null if no supplier for the value is present.
     */
    @RestrictedApi(explanation = "Unsafe method for resolving references inside the holder, this should ABSOLUTELY NOT BE USED IN PRODUCTION" +
            " this should be used only for data generation by dedicated data providers.", allowlistAnnotations = DataGenProvider.class)
    public @Nullable T resolve() {
        if (this.referencePresent())
            return this.get();

        return this.supplier != null ? this.supplier.get() : null;
    }

    /**
     * Retrieve the key of the object.
     *
     * @return key of the object.
     */
    @NotNull
    public Key key() {
        return this.key;
    }

    /**
     * Retrieve the reference of this object as an optional.
     *
     * @return optional containing the reference of this object.
     */
    @SuppressWarnings("unchecked")
    public Optional<T> asOptional() {
        if (this.registry == null)
            return Optional.empty();

        return this.registry.retrieveValue(this.key).map(value -> (T) value);
    }
}