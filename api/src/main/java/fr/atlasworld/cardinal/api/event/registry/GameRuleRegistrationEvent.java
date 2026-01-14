package fr.atlasworld.cardinal.api.event.registry;

import fr.atlasworld.cardinal.api.game.GameRule;
import fr.atlasworld.cardinal.api.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when registering {@link GameRule game rules}.
 */
public final class GameRuleRegistrationEvent extends RegistrationEvent<GameRule<?>> {
    @ApiStatus.Internal
    public GameRuleRegistrationEvent(@NotNull Registry<GameRule<?>> registry) {
        super(registry);
    }
}
