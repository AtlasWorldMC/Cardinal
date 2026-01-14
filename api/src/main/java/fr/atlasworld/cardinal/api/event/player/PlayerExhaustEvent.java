package fr.atlasworld.cardinal.api.event.player;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.trait.CardinalPlayerEvent;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.minestom.server.event.trait.CancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the player is getting exhausted.
 */
public class PlayerExhaustEvent implements CardinalPlayerEvent, CancellableEvent {
    private final CardinalPlayer player;

    private float exhaustion;
    private boolean cancelled;

    public PlayerExhaustEvent(@NotNull CardinalPlayer player, float exhaustion) {
        Preconditions.checkNotNull(player, "Player cannot be null!");

        this.player = player;
        this.exhaustion = exhaustion;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * {@inheritDoc}
     * @param cancel {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public @NotNull CardinalPlayer getPlayer() {
        return this.player;
    }

    /**
     * The exhaustion that will be applied to the player.
     *
     * @return the exhaustion to be applied
     */
    public float exhaustion() {
        return this.exhaustion;
    }

    /**
     * Set the exhaustion to be applied to the player.
     *
     * @param exhaustion exhaustion to be applied.
     */
    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }
}
