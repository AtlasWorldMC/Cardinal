package fr.atlasworld.cardinal.api.event.trait;

import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Specialized player event which gives access to the {@link CardinalPlayer}.
 */
public interface CardinalPlayerEvent extends PlayerEvent {

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    @NotNull CardinalPlayer getPlayer();
}
