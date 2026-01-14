package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.FightEngineRegistrationEvent;
import fr.atlasworld.cardinal.api.game.GameFightEngine;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents all the {@link GameFightEngine}s registered by cardinal.
 */
public final class CardinalFightEngines {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<GameFightEngine> FIGHT_ENGINES_REGISTER = DELEGATE.createInternalRegister(FightEngineRegistrationEvent.class);

    public static final RegistryHolder<GameFightEngine.ModernFightEngine> MODERN = FIGHT_ENGINES_REGISTER.register("modern", GameFightEngine.ModernFightEngine::new);
    public static final RegistryHolder<GameFightEngine.LegacyFightEngine> LEGACY = FIGHT_ENGINES_REGISTER.register("legacy", GameFightEngine.LegacyFightEngine::new);

    private CardinalFightEngines() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
