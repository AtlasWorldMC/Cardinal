package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.WorldFormatRegistrationEvent;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.world.GameWorldFormat;
import org.jetbrains.annotations.ApiStatus;

public final class CardinalWorldFormats {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<GameWorldFormat> FORMATS = DELEGATE.createInternalRegister(WorldFormatRegistrationEvent.class);

    public static final RegistryHolder<GameWorldFormat> POLAR = FORMATS.register("polar", DELEGATE::retrievePolarWorldFormat);
    // TODO: Implement Anvil World format.

    private CardinalWorldFormats() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
