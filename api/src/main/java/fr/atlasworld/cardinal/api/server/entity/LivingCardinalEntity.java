package fr.atlasworld.cardinal.api.server.entity;

import fr.atlasworld.cardinal.api.server.item.ItemCooldownManager;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Special implementation of {@link LivingEntity} which adds more cardinal specific methods.
 */
public class LivingCardinalEntity extends LivingEntity implements CardinalEntity {
    protected final ItemCooldownManager cooldownManager;

    public LivingCardinalEntity(@NotNull EntityType entityType, @NotNull UUID uuid) {
        super(entityType, uuid);

        this.cooldownManager = createCooldownManager();
    }

    public LivingCardinalEntity(@NotNull EntityType entityType) {
        this(entityType, UUID.randomUUID());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isHurt() {
        return !this.isDead() && this.getHealth() < this.getAttributeValue(Attribute.MAX_HEALTH);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Sound.@NotNull Source getSoundSource() {
        return Sound.Source.NEUTRAL;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public @Nullable SoundEvent getHurtSound() {
        return SoundEvent.ENTITY_GENERIC_HURT;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public @NotNull ItemCooldownManager cooldownManager() {
        return this.cooldownManager;
    }

    /**
     * Create the {@link ItemCooldownManager} for this entity.
     *
     * @return newly created item cooldown manager.
     */
    public @NotNull ItemCooldownManager createCooldownManager() {
        return new ItemCooldownManager(this);
    }
}
