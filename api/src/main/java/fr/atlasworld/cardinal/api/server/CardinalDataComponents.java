package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.DataComponentRegistrationEvent;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.component.ServerDataComponent;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents all the {@link ServerDataComponent}s registered by cardinal.
 */
public final class CardinalDataComponents {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<ServerDataComponent<?>> COMPONENTS = DELEGATE.createInternalRegister(DataComponentRegistrationEvent.class);

    public static final RegistryHolder<ServerDataComponent<Key>> BLOCK_ITEM_BLOCK =
            COMPONENTS.register("block_item_block", () -> new ServerDataComponent<>(Codec.KEY));

    private CardinalDataComponents() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
