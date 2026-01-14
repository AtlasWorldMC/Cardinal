package fr.atlasworld.cardinal.api.event.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.trait.GameEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a new game is created.
 */
public class GameCreatedEvent implements GameEvent {
    private final GameContainer container;

    public GameCreatedEvent(@NotNull GameContainer container) {
        Preconditions.checkNotNull(container, "Game container cannot be null!");
        this.container = container;
    }

    @Override
    public @NotNull GameContainer container() {
        return this.container;
    }
}
