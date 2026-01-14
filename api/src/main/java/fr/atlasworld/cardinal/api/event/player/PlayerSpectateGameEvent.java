package fr.atlasworld.cardinal.api.event.player;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.trait.CardinalPlayerEvent;
import fr.atlasworld.cardinal.api.event.trait.GameEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Event called when a player is added as a spectator to a running game.
 *
 * @see PlayerJoinGameEvent
 */
public class PlayerSpectateGameEvent implements GameEvent, PlayerInstanceEvent, CardinalPlayerEvent {
    private final GameContainer container;
    private final CardinalPlayer player;

    public PlayerSpectateGameEvent(@NotNull GameContainer container, @NotNull CardinalPlayer player) {
        Preconditions.checkNotNull(container, "Game container cannot be null!");
        Preconditions.checkNotNull(player, "Player cannot be null!");

        this.container = container;
        this.player = player;
    }

    @Override
    public @NotNull GameContainer container() {
        return this.container;
    }

    @Override
    public @NotNull CardinalPlayer getPlayer() {
        return this.player;
    }
}
