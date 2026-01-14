package fr.atlasworld.cardinal.api.event.game;

import fr.atlasworld.cardinal.api.event.trait.GameEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a game changes its state to a different one.
 */
public class GameStateChangeEvent implements GameEvent {
    private final GameContainer container;
    private final GameContainer.State oldState;

    public GameStateChangeEvent(GameContainer container, GameContainer.State oldState) {
        this.container = container;
        this.oldState = oldState;
    }

    @Override
    public @NotNull GameContainer container() {
        return this.container;
    }

    /**
     * Retrieve the previous state of the container.
     *
     * @return previous state of the container.
     */
    public @NotNull GameContainer.State oldState() {
        return this.oldState;
    }

    /***
     * Retrieve the new and current state of the container.
     *
     * @return current state of the container.
     */
    public @NotNull GameContainer.State newState() {
        return this.container.state();
    }
}
