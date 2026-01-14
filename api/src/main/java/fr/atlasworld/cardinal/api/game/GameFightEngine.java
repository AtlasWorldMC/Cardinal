package fr.atlasworld.cardinal.api.game;

import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.utils.MathUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Fight engine for a game, this allows a game to specify their own PvP and PvE specifications
 */
public interface GameFightEngine {

    /**
     * Calculates and returns a value between 0.0 and 1.0 that represents the cooldown progress of a player.
     * <p>
     * This can be used to disable any cooldown between attacks, returning {@code 1.0} will always attack the target with the maximum damage.
     *
     * @param player player to compute the cooldown progress for.
     * @return a value between 0.0 and 1.0.
     */
    double playerAttackCooldownProgress(CardinalPlayer player);

    /**
     * Whether to cancel the player's sprinting after an attack.
     *
     * @return {@code true} to cancel the sprinting, {@code false} otherwise.
     */
    boolean cancelSprintingAfterAttack();

    /**
     * Returns the minimum time (in ticks) between attacks.
     * <p>
     * Any attacks triggered before this time will be canceled.
     *
     * @return minimum time between attacks.
     */
    default int attackDeadTime() {
        return 8;
    }

    /**
     * Calculates the additional damages a critical hit will do.
     *
     * @param baseDamage base attack damage.
     * @return modified damage value.
     */
    float criticalDamageModifier(float baseDamage);

    /**
     * Retrieve the attack exhaustion value.
     * <br>
     * This is used when calculating exhaustion for the food system when attacking.
     * Higher exhaustion means the food will run out quicker.
     *
     * @return attack exhaustion value.
     */
    float attackExhaustion();

    /**
     * Retrieve the damage exhaustion value.
     * <br>
     * This is used when calculating exhaustion for the food system when taking damage.
     * Higher exhaustion means the food will run out quicker.
     *
     * @return damage exhaustion value.
     */
    float damageExhaustion();

    /**
     * Modern (Post 1.9 Minecraft) fight engine.
     * <br><br>
     * This is the <b>default fight engine</b> used by games which don't specify one.
     */
    class ModernFightEngine implements GameFightEngine {

        @Override
        public double playerAttackCooldownProgress(CardinalPlayer player) {
            double attackSpeed = player.getAttributeValue(Attribute.ATTACK_SPEED);
            double cooldownProgressPerTick = (1 / attackSpeed) * 20;
            long lastTimeSinceAttack = player.getAliveTicks() - player.lastAttackTime();

            return MathUtils.clamp(((lastTimeSinceAttack + 0.5) / cooldownProgressPerTick), 0, 1);
        }

        @Override
        public boolean cancelSprintingAfterAttack() {
            return true;
        }

        @Override
        public float criticalDamageModifier(float baseDamage) {
            return baseDamage + 1.5f;
        }

        @Override
        public float attackExhaustion() {
            return 0.1f;
        }

        @Override
        public float damageExhaustion() {
            return 1f;
        }
    }

    /**
     * Legacy (Pre 1.9 Minecraft) fight engine, which does not have an attack cooldown.
     */
    class LegacyFightEngine implements GameFightEngine {
        @Override
        public double playerAttackCooldownProgress(CardinalPlayer player) {
            return 1.0;
        }

        @Override
        public boolean cancelSprintingAfterAttack() {
            return false;
        }

        @Override
        public float criticalDamageModifier(float baseDamage) {
            return baseDamage + ThreadLocalRandom.current().nextInt((int) (baseDamage / 2 + 2));
        }

        @Override
        public float attackExhaustion() {
            return 0.3f;
        }

        @Override
        public float damageExhaustion() {
            return 1f;
        }
    }
}
