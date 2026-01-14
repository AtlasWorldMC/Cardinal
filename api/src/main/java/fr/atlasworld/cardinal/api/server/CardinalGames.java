package fr.atlasworld.cardinal.api.server;

import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.delegate.RegistryDelegate;
import fr.atlasworld.cardinal.api.event.registry.GameRegistrationEvent;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.builtin.LimboGameLogic;
import fr.atlasworld.cardinal.api.game.builtin.TestEnvLogic;
import fr.atlasworld.cardinal.api.registry.Register;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;

public final class CardinalGames {
    private static final RegistryDelegate DELEGATE = DelegateFactory.registryDelegate();
    private static final Register<Game> GAMES = DELEGATE.createInternalRegister(GameRegistrationEvent.class);

    public static final RegistryHolder<Game> LIMBO = GAMES.register("limbo", () -> DELEGATE.buildInternalGame(
            Game.builder().canShareMap(true).displayName(Component.text("Limbo")).logic(LimboGameLogic::new)
                    .dimension(LimboGameLogic.DIM_IDENTIFIER, CardinalDimensionTypes.LIMBO)));

    public static final RegistryHolder<Game> TEST = GAMES.register("test", () -> DELEGATE.buildInternalGame(
            Game.builder().canShareMap(false).displayName(Component.text("Test Environment")).logic(TestEnvLogic::new)
                    .dimension(TestEnvLogic.PRIMARY_DIM_IDENTIFIER, CardinalDimensionTypes.OVERWORLD)
                    .dimension(TestEnvLogic.SECONDARY_DIM_IDENTIFIER, CardinalDimensionTypes.OVERWORLD)));

    private CardinalGames() {}

    /**
     * Does nothing, simply here to load static fields.
     */
    @ApiStatus.Internal
    public static void init() {
    }
}
