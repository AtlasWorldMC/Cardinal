package fr.atlasworld.cardinal.api.event.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.trait.RegistryEvent;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

/**
 * Generic registration event.
 * <p>
 * Extend this class for your custom registration event.
 *
 * @param <T> type of the registry.
 */
public abstract class RegistrationEvent<T> implements RegistryEvent<T> {
    protected final @NotNull Registry<T> registry;

    protected RegistrationEvent(@NotNull Registry<T> registry) {
        Preconditions.checkNotNull(registry, "Registry cannot be null");
        this.registry = registry;
    }

    @Override
    public @NotNull Registry<T> registry() {
        return this.registry;
    }

    /**
     * Register a single element to the registry.
     *
     * @param key   key of the element.
     * @param value element to register.
     * @throws IllegalArgumentException if a value was already registered with the specified key,
     *                                  or that the same instance of the value is already registered.
     */
    public void register(@NotNull RegistryKey key, @NotNull T value) {
        this.registry.register(key, value);
    }

    /**
     * Register a {@link Register} for bulk registering.
     *
     * @param register {@link Register} to register.
     */
    public void register(@NotNull Register<T> register) {
        register.register(this.registry);
    }
}
