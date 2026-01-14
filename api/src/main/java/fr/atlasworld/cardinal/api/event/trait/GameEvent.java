package fr.atlasworld.cardinal.api.event.trait;

import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.game.GameMap;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any event called about games.
 */
public interface GameEvent extends Event {

    /**
     * Game container, in which the event happened.
     *
     * @return game container.
     */
    @NotNull GameContainer container();

    /**
     * Game currently running in the container.
     *
     * @return game running in the container.
     */
    default @NotNull Game game() {
        return this.container().game();
    }

    /**
     * Map currently used by the container.
     *
     * @return map currently used by the container?
     */
    default @NotNull GameMap map() {
        return this.container().map();
    }
}
