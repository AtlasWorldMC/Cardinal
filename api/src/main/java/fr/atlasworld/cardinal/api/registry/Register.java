package fr.atlasworld.cardinal.api.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.registry.RegistrationEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility class for mass-registering.
 *
 * @param <T> type of element to register.
 */
public final class Register<T> {
    private final String namespace;

    private final Map<Key, Supplier<? extends T>> registeredValues;
    private final List<RegistryHolder<? extends T>> registeredHolders;

    private boolean registered;

    public Register(@NotNull String namespace) {
        Preconditions.checkArgument(RegistryKey.isValidNamespace(namespace), "Invalid namespace, must be [a-z0-9._-]: %s", namespace);

        this.namespace = namespace;

        this.registeredValues = new HashMap<>();
        this.registeredHolders = new ArrayList<>();

        this.registered = false;
    }

    public Register(@NotNull Namespaced namespaced) {
        this(namespaced.namespace());
    }

    /**
     * Registers an element to the register.
     *
     * @param name        name of the element must also be a valid key for a {@link RegistryKey}.
     * @param constructor supplier that will supply the object when the register is called for registering.
     *
     * @return registry object for the registered object,
     * reference will only be provided when the register is called for registering.
     *
     * @throws IllegalArgumentException if the name is not a valid key for a {@link RegistryKey}.
     * @throws IllegalStateException    if an element with the provided name has already been registered before.
     */
    public <V extends T> RegistryHolder<V> register(@NotNull String name, @NotNull Supplier<V> constructor) {
        Preconditions.checkArgument(RegistryKey.isValidKey(name), "Invalid name, must be [a-z0-9/._-]: %s", name);
        Preconditions.checkNotNull(constructor);
        Preconditions.checkState(!this.registered, "Cannot add entries to a register after it was registered.");

        RegistryKey key = new RegistryKey(this.namespace, name);
        if (this.registeredValues.containsKey(key))
            throw new IllegalStateException("Element with this name has already been registered.");

        RegistryHolder<V> holder = new RegistryHolder<>(key, constructor);

        this.registeredValues.put(key, constructor);
        this.registeredHolders.add(holder);

        return holder;
    }

    /**
     * Register the elements of this register to the provided registry.
     *
     * @param registry registry to register the elements to.
     * @throws IllegalStateException if the register has already been registered.
     */
    public synchronized void register(@NotNull Registry<T> registry) {
        Preconditions.checkNotNull(registry);

        if (!this.registered) {
            this.registered = true;
            this.registeredHolders.forEach(holder -> holder.updateRegistry(registry));
        }

        this.registeredValues.forEach((key, sup) -> {
            T value = sup.get();
            registry.register(key, value);
        });
    }

    /**
     * Register the elements of this register to the provided registry.
     *
     * @param event event containing the registry.
     * @throws IllegalStateException if the register has already been registered.
     */
    public void register(@NotNull RegistrationEvent<T> event) {
        Preconditions.checkNotNull(event);
        event.register(this);
    }
}
