package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when registering {@link Game games}.
 */
public final class GameRegistrationEvent extends RegistrationEvent<Game> {
    @ApiStatus.Internal
    public GameRegistrationEvent(@NotNull Registry<Game> registry) {
        super(registry);
    }
}
