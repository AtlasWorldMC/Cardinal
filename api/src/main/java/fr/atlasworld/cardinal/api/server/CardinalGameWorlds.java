package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.WorldRegistrationEvent;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.server.world.generator.FlatWorldGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static fr.atlasworld.cardinal.api.registry.CardinalRegistries.NAMESPACE;

public final class CardinalGameWorlds {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<GameWorld> WORLDS = DELEGATE.createInternalRegister(WorldRegistrationEvent.class);

    // Loaded
    public static final RegistryHolder<GameWorld> LIMBO = of("limbo");

    // Generators
    public static final RegistryHolder<FlatWorldGenerator> FLAT = WORLDS.register("flat", FlatWorldGenerator::new);

    private CardinalGameWorlds() {}

    private static RegistryHolder<GameWorld> of(@NotNull String name) {
        return CardinalRegistries.WORLDS.retrieveHolder(new RegistryKey(NAMESPACE, name));
    }

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
