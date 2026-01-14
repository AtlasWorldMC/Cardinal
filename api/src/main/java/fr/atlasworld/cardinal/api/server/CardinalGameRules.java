package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.GameRuleRegistrationEvent;
import fr.atlasworld.cardinal.api.game.GameFightEngine;
import fr.atlasworld.cardinal.api.game.GameRule;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents all the {@link GameRule}s registered by cardinal.
 */
public final class CardinalGameRules {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<GameRule<?>> GAME_RULES = DELEGATE.createInternalRegister(GameRuleRegistrationEvent.class);

    public static final RegistryHolder<GameRule<Boolean>> HUNGER = GAME_RULES.register("hunger", () -> GameRule.ofBoolean(true));
    public static final RegistryHolder<GameRule<Boolean>> CAN_STARVE = GAME_RULES.register("can_starve", () -> GameRule.ofBoolean(true));
    public static final RegistryHolder<GameRule<Double>> JUMP_EXHAUSTION_MULTIPLIER = GAME_RULES.register("jump_exhaustion_multiplier", () -> GameRule.ofDouble(1D));

    public static final RegistryHolder<GameRule<Boolean>> NATURAL_REGENERATION = GAME_RULES.register("natural_regeneration", () -> GameRule.ofBoolean(true));
    public static final RegistryHolder<GameRule<GameFightEngine>> PVP = GAME_RULES.register("pvp", () -> GameRule.ofRegistry(CardinalRegistries.FIGHT_ENGINES, CardinalFightEngines.MODERN.get()));

    private CardinalGameRules() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
