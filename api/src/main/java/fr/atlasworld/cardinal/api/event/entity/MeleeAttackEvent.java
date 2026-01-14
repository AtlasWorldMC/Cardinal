package fr.atlasworld.cardinal.api.event.entity;

import com.google.common.base.Preconditions;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.EntityInstanceEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a living entity melees another living entity.
 * <br><br>
 * This event is called after the enchantments an attributes values have been computed.
 */
public class MeleeAttackEvent implements EntityInstanceEvent, ItemEvent, CancellableEvent {
    private final LivingEntity attacker;
    private final LivingEntity target;
    private final Damage damageSource;

    private float damage;
    private float knockback;

    private boolean strongAttack;
    private boolean sprintAttack;
    private boolean criticalAttack;
    private boolean sweepAttack;

    private boolean cancelled;

    public MeleeAttackEvent(@NotNull LivingEntity attacker, @NotNull LivingEntity target, @NotNull Damage damageSource,
                            float damage, float knockback, boolean strongAttack, boolean sprintAttack, boolean criticalAttack, boolean sweepAttack) {
        Preconditions.checkNotNull(attacker, "Source cannot be null!");
        Preconditions.checkNotNull(target, "Target cannot be null!");
        Preconditions.checkNotNull(damageSource, "Damage cannot be null!");

        this.attacker = attacker;
        this.target = target;
        this.damageSource = damageSource;

        this.damage = damage;
        this.knockback = knockback;

        this.strongAttack = strongAttack;
        this.sprintAttack = sprintAttack;
        this.criticalAttack = criticalAttack;
        this.sweepAttack = sweepAttack;

        this.cancelled = false;
    }

    /**
     * Retrieve the damage details that the target will receive.
     *
     * @return damage details.
     */
    public @NotNull Damage getDamageSource() {
        return this.damageSource;
    }

    /**
     * Retrieve the attacker.
     *
     * @return the attacker.
     */
    public @NotNull LivingEntity getAttacker() {
        return this.attacker;
    }

    /**
     * Retrieve the attacker.
     *
     * @return the attacker.
     */
    @Override
    public @NotNull Entity getEntity() {
        return this.attacker;
    }

    /**
     * Retrieve the target / victim that will receive the damage.
     *
     * @return target of the attack.
     */
    public @NotNull LivingEntity getTarget() {
        return this.target;
    }

    /**
     * Whether the attack is canceled.
     *
     * @return {@code true} if the attack is canceled, {@code false} otherwise.
     */
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Set whether to cancel the attack.
     *
     * @param cancel {@code true} if the event should be canceled, {@code false} otherwise
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return this.attacker.getItemInMainHand();
    }

    /**
     * Get the damage value.
     *
     * @return damage value.
     */
    public float getDamage() {
        return this.damage;
    }

    /**
     * Set the damage value.
     *
     * @param damage damage value to set.
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }

    /**
     * Get the knockback value.
     *
     * @return knockback value.
     */
    public float getKnockback() {
        return this.knockback;
    }

    /**
     * Whether the attack is a strong attack.
     * <br>
     * This means that the player attacker once the cooldown has been completed.
     *
     * @return {@code true} if the attack is a strong attack, {@code false} otherwise.
     */
    public boolean isStrongAttack() {
        return this.strongAttack;
    }

    /**
     * Set whether the attack is a strong attack.
     * <br>
     * This means that the player attacker once the cooldown has been completed.
     *
     * @param strongAttack whether the attack is a strong attack.
     */
    public void setStrongAttack(boolean strongAttack) {
        this.strongAttack = strongAttack;
    }

    /**
     * Whether the attack is a sprint attack.
     * <br>
     * This means that the player attacker once the cooldown has been completed and was sprinting.
     *
     * @return {@code true} if the attack is a sprint attack, {@code false} otherwise.
     */
    public boolean isSprintAttack() {
        return this.sprintAttack;
    }

    /**
     * Set whether the attack is a sprint attack.
     * <br>
     * This means that the player attacker once the cooldown has been completed and was sprinting.
     *
     * @param sprintAttack sprint attack.
     */
    public void setSprintAttack(boolean sprintAttack) {
        this.sprintAttack = sprintAttack;
    }

    /**
     * Whether the attack is a critical attack.
     *
     * @return {@code true} if the attack is critical, {@code false} otherwise.
     */
    public boolean isCriticalAttack() {
        return this.criticalAttack;
    }

    /**
     * Sets whether the attack is a critical attack.
     *
     * @param criticalAttack critical attack.
     */
    public void setCriticalAttack(boolean criticalAttack) {
        this.criticalAttack = criticalAttack;
    }

    /**
     * Whether the attack is a sweep attack.
     * <br>
     * Sweeping attacks define whether enchantments like sweeping edge should be called.
     *
     * @return {@code true} if the attack is a sweep attack, {@code false} otherwise.
     */
    public boolean isSweepAttack() {
        return this.sweepAttack;
    }

    /**
     * Set whether the attack is a sweep attack.
     * <br>
     * Sweeping attacks define whether enchantments like sweeping edge should be called.
     *
     * @param sweepAttack sweep attack.
     */
    public void sweepAttack(boolean sweepAttack) {
        this.sweepAttack = sweepAttack;
    }

    /**
     * Set the knockback value.
     *
     * @param knockback knockback value to set.
     */
    @ApiStatus.Experimental
    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }
}
