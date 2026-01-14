package fr.atlasworld.cardinal.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.registry.RegistrationEvent;
import fr.atlasworld.cardinal.api.event.registry.RegistryRegistrationEvent;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.registry.ReloadableRegistry;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public final class Registries extends CardinalRegistry<Registries.RegistryHolder<?>> implements RegistryRegistrationEvent.RegistryRegistrationDelegate {
    private static final Logger LOGGER = Logging.logger();
    private final AtomicInteger counter = new AtomicInteger(0); // Used to maintain order of registries.

    public Registries(@NotNull RegistryKey key) {
        super(key);
    }

    @Override
    public <T> void registerRegistry(@NotNull Registry<T> registry, @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier) {
        Preconditions.checkNotNull(registry);

        RegistryHolder<T> holder = new RegistryHolder<>(registry, eventSupplier, this.counter.getAndIncrement());
        this.register(registry.registryKey(), holder);
    }

    public synchronized void callRegistries() {
        if (!this.frozen())
            throw new IllegalStateException("Cannot call registered registries when the Registry Registry isn't frozen.");

        for (RegistryHolder<?> holder : this.getSortedRegistries()) {
            if (holder.registry().frozen()) {
                LOGGER.warn("Registry {} was frozen and it's impossible to register elements to it.", holder.registry().registryKey());
                continue;
            }

            RegistrationEvent<?> event = holder.retrieveEvent();
            if (event != null)
                EventDispatcher.call(event);
        }
    }

    public synchronized void freezeRegistries() {
        if (!this.frozen())
            throw new IllegalStateException("Cannot freeze registered registries when the Registry Registry isn't frozen.");

        for (RegistryHolder<?> holder : this.getSortedRegistries()) {
            if (!holder.registry().frozen())
                holder.registry().freezeRegistry();
        }
    }

    public synchronized void reloadRegistries() {
        if (!this.frozen())
            throw new IllegalStateException("Cannot reload registered registries when the Registry Registry isn't frozen.");

        List<RegistryHolder<?>> registries = this.getSortedRegistries();

        // Unfreeze registries and clear their elements
        for (RegistryHolder<?> holder : registries) {
            Registry<?> registry = holder.registry();
            if (registry instanceof ReloadableRegistry<?> reloadableRegistry)
                reloadableRegistry.reload();
        }
    }

    private List<RegistryHolder<?>> getSortedRegistries() {
        return this.entries.values().stream()
                .sorted(Comparator.comparingInt(RegistryHolder::index))
                .toList();
    }

    public record RegistryHolder<T>(@NotNull Registry<T> registry,
                                    @Nullable Function<Registry<T>, RegistrationEvent<T>> eventSupplier, int index) {
        public @Nullable RegistrationEvent<T> retrieveEvent() {
            if (this.eventSupplier == null)
                return null;

            return this.eventSupplier.apply(this.registry);
        }
    }
}
