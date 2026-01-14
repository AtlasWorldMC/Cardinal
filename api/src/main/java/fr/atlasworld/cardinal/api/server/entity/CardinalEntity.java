package fr.atlasworld.cardinal.api.server.entity;

import fr.atlasworld.cardinal.api.server.item.ItemCooldownManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interfaced aimed at custom {@link net.minestom.server.entity.LivingEntity} implementations, with additional functionalities.
 */
public interface CardinalEntity {

    /**
     * Helper method to retrieve the sound source of an entity.
     *
     * @param entity entity to retrieve the sound source from.
     * @return sound source of the entity.
     */
    static Sound.Source retrieveSoundSource(Entity entity) {
        return entity instanceof CardinalEntity cardinalEntity ? cardinalEntity.getSoundSource() : Sound.Source.NEUTRAL;
    }

    /**
     * Helper method to retrieve the sound to play when the entity gets hurt.
     *
     * @param entity entity to retrieve the sound from.
     * @return hurt sound of the entity.
     */
    static SoundEvent retrieveHurtSound(Entity entity) {
        return entity instanceof CardinalEntity cardinalEntity ? cardinalEntity.getHurtSound() : SoundEvent.ENTITY_GENERIC_HURT;
    }

    /**
     * Checks whether the entity is hurt.
     *
     * @return {@code true} if the entity is hurt, {@code false} otherwise.
     */
    boolean isHurt();

    /**
     * Get the sound source of the entity.
     * <br>
     * This will is used to associate the sound with a certain group that can be disabled in the player's audio settings.
     *
     * @return the sound source of the entity.
     */
    @NotNull Sound.Source getSoundSource();

    /**
     * Sound to play when the entity gets hurt.
     *
     * @return sound to play when the entity gets hurt.
     */
    @Nullable SoundEvent getHurtSound();

    /**
     * Retrieve the item cooldown manager associated with the entity.
     *
     * @return item cooldown manager associated with the entity.
     */
    @NotNull ItemCooldownManager cooldownManager();
}
