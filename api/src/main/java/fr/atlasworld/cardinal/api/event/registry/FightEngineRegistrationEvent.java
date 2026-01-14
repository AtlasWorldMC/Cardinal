package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.game.GameFightEngine;
import fr.atlasworld.cardinal.api.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when registering {@link GameFightEngine game fight engines}.
 */
public final class FightEngineRegistrationEvent extends RegistrationEvent<GameFightEngine> {
    @ApiStatus.Internal
    public FightEngineRegistrationEvent(@NotNull Registry<GameFightEngine> registry) {
        super(registry);
    }
}
