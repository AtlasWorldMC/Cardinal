package fr.atlasworld.cardinal.api.event.entity;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.trait.CardinalItemEvent;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link LivingEntity} blocks damage using an item capable of blocking damage. (Like a shield).
 */
public class EntityBlockDamageEvent implements EntityInstanceEvent, CardinalItemEvent, CancellableEvent {
    private final LivingEntity entity;
    private final PlayerHand hand;
    private final ItemStack stack;
    private final Damage source;

    private float blockedAmount;
    private boolean cancelled;

    public EntityBlockDamageEvent(@NotNull LivingEntity entity, @NotNull PlayerHand hand, @NotNull ItemStack stack, @NotNull Damage source, float blockedAmount) {
        Preconditions.checkNotNull(entity, "Entity cannot be null!");
        Preconditions.checkNotNull(hand, "Hand cannot be null!");
        Preconditions.checkNotNull(stack, "Stack cannot be null!");
        Preconditions.checkNotNull(source, "Damage cannot be null!");

        this.entity = entity;
        this.hand = hand;
        this.stack = stack;
        this.source = source;

        this.blockedAmount = blockedAmount;

        this.cancelled = false;
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
     *
     * @param cancel {@inheritDoc}
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public @NotNull LivingEntity getEntity() {
        return this.entity;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public @NotNull ItemStack getItemStack() {
        return this.stack;
    }

    /**
     * Retrieve the hand that was holding the blocking item.
     *
     * @return the player hand used.
     */
    public @NotNull PlayerHand hand() {
        return this.hand;
    }

    /**
     * Retrieve the damage source that was blocked.
     *
     * @return the damage source.
     */
    public @NotNull Damage damageSource() {
        return this.source;
    }

    /**
     * Retrieve how much damage will be blocked.
     * <br><br>
     * This value will be subtracted from the total damage amount.
     *
     * @return the amount of damage blocked.
     */
    public float damageBlocked() {
        return this.blockedAmount;
    }

    /**
     * Sets how much damage will be blocked.
     * <br><br>
     * This value will be subtracted from the total damage amount,
     * so putting more or the same damage reduction as the amount of damage will effectively cancel the damage.
     *
     * @param amount amount of to block.
     */
    public void setDamageBlocked(float amount) {
        this.blockedAmount = Math.max(0, amount);
    }
}
