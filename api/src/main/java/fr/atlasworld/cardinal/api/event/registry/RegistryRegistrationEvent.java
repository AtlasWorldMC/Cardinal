package fr.atlasworld.cardinal.api.event.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.Registry;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Event called when cardinal registers custom {@link Registry registries}.
 */
public final class RegistryRegistrationEvent implements Event {
    private final RegistryRegistrationDelegate delegate;

    @ApiStatus.Internal
    public RegistryRegistrationEvent(@NotNull RegistryRegistrationDelegate delegate) {
        Preconditions.checkNotNull(delegate, "Delegate cannot be null");
        this.delegate = delegate;
    }

    /**
     * Register a registry to cardinal.
     * <br><br>
     * If the registry is a {@link fr.atlasworld.cardinal.api.registry.ReloadableRegistry} cardinal will automatically
     * handle the reloading and refreezing of the registry.
     * <br><br>
     * The order that you register your registries will also be the order that they will be called for registering.
     * So if you want to make sure your registries are called after or before the ones of another plugin, use the
     * {@link EventNode#getPriority()}. To set a higher or lower priority depending on your need.
     * <br>
     * Cardinal Registries will always be registered first before any other,
     * keep that in mind when you work with registries.
     *
     * @param registry      registry to register.
     * @param eventSupplier supplier that will supply the event when the registry is called for registering.
     * @param <T>           value type to register.
     * @throws IllegalArgumentException if the registry is frozen or that the registry is already registered.
     */
    public <T> void register(@NotNull Registry<T> registry, @NotNull Function<Registry<T>, RegistrationEvent<T>> eventSupplier) {
        Preconditions.checkNotNull(registry, "Registry cannot be null");
        Preconditions.checkNotNull(eventSupplier, "Event supplier cannot be null");

        Preconditions.checkArgument(!registry.frozen(), "Cannot register a frozen registry! A plugin should not freeze registries.");

        this.delegate.registerRegistry(registry, eventSupplier);
    }

    @ApiStatus.Internal
    public interface RegistryRegistrationDelegate {
        <T> void registerRegistry(@NotNull Registry<T> registry, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier);
    }
}
