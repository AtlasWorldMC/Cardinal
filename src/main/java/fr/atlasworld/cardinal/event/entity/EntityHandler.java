package fr.atlasworld.cardinal.event.entity;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.event.entity.EntityBlockDamageEvent;
import fr.atlasworld.cardinal.api.event.entity.MeleeAttackEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.game.GameFightEngine;
import fr.atlasworld.cardinal.api.server.CardinalFightEngines;
import fr.atlasworld.cardinal.api.server.CardinalGameRules;
import fr.atlasworld.cardinal.api.server.entity.CardinalEntity;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.util.MathUtils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.LivingEntityMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class EntityHandler {
    public static void onEntityAttack(EntityAttackEvent event) {
        // TODO: Implement handler for projectile entities.
        prepareMeleeAttack(event);
    }

    public static void onEntityDamage(EntityDamageEvent event) {
        LivingEntity entity = event.getEntity();
        Damage source = event.getDamage();

        if (event.getDamage().getAmount() < 0.0F)
            event.getDamage().setAmount(0.0F);

        float blockedDamageReduction = getAmountDamageBlocked(entity, source);
        boolean blockedDamage = blockedDamageReduction > 0.0F;
        source.setAmount(source.getAmount() - blockedDamageReduction);

        // TODO: Add a way for entities to take additional damage depending on their state.

        if (Float.isNaN(source.getAmount()) || Float.isInfinite(source.getAmount()))
            source.setAmount(Float.MAX_VALUE);

        // TODO: Implement healing system

        // Knockback
        if (source.getSourcePosition() != null) {
            double x = source.getSourcePosition().x() - entity.getPosition().x();
            double z = source.getSourcePosition().z() - entity.getPosition().z();
            entity.takeKnockback(0.4F, x, z);
        }

        if (blockedDamage) {
            if (source.getAmount() <= 0.0F)
                event.setCancelled(true);

            postDamageBlock(entity, source, blockedDamageReduction);
        }

        if (source.getAmount() > 0 && entity instanceof CardinalPlayer player) {
            player.gameContainer().ifPresent(container -> {
                DamageType type = MinecraftServer.getDamageTypeRegistry().get(source.getType());
                GameFightEngine engine = container.getRuleValueOrDefault(CardinalGameRules.PVP.get(), CardinalFightEngines.MODERN.get());

                player.addExhaustion(type.exhaustion() * engine.damageExhaustion());
            });
        }

        float armor = (float) entity.getAttributeValue(Attribute.ARMOR);
        float armorToughness = (float) entity.getAttributeValue(Attribute.ARMOR_TOUGHNESS);

        float f = 2.0F + armorToughness / 4.0F;
        float g = MathUtils.clamp(armor - source.getAmount() / f, armor * 0.2F, 20.0F);
        float h = g / 25.0F;

        source.setAmount((float) (source.getAmount() * (1.0 - h)));
        event.setSound(entity instanceof CardinalEntity cardinalEntity ? cardinalEntity.getHurtSound() : null);
    }

    private static void prepareMeleeAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof LivingEntity attacker) || !(event.getTarget() instanceof LivingEntity target)) {
            attackFail(event.getInstance(), event.getEntity());
            return;
        }

        Instance instance = event.getInstance();
        GameContainer container = CardinalServer.instance().gameManager().retrieveAssociatedContainer(instance).orElseThrow(() -> new IllegalStateException("Instance is not linked to any games!"));
        GameFightEngine engine = container.getRuleValueOrDefault(CardinalGameRules.PVP.get(), CardinalFightEngines.MODERN.get());

        double attackCooldown = 1.0;
        if (attacker instanceof CardinalPlayer player) {
            long lastAttackTime = player.getAliveTicks() - player.lastAttackTime();
            attackCooldown = engine.playerAttackCooldownProgress(player);

            // TODO: Replace with the health system, in which a cooldown should be applied if the previous damage isn't greater than the new attack.
            if (lastAttackTime < engine.attackDeadTime()) {
                attackFail(instance, target);
                return;
            }

            player.resetAttackCooldown();
        }

        boolean strongAttack = attackCooldown > 0.9F;
        boolean sprintingAttack = attacker.isSprinting() && strongAttack;
        boolean criticalAttack = isCriticalAttack(attacker) && strongAttack;
        boolean sweepingAttack = strongAttack && !sprintingAttack && !criticalAttack && isSweeping(attacker);

        ItemStack stack = attacker.getItemInMainHand();
        Optional<CardinalItem> item = CardinalItem.fromStack(stack);

        Damage damageSource = item.map(cardinalItem -> cardinalItem.damageSource(attacker))
                .orElse(new Damage(attacker instanceof Player ? DamageType.PLAYER_ATTACK : DamageType.MOB_ATTACK, attacker, attacker, attacker.getPosition(), 0));

        float baseDamage = (float) attacker.getAttributeValue(Attribute.ATTACK_DAMAGE);
        baseDamage *= (float) (0.2F + attackCooldown * attackCooldown * 0.8F);

        float additionalDamage = 0.0F;
        if (item.isPresent())
            additionalDamage = item.get().getBonusAttackDamage(attacker, target, baseDamage, damageSource, stack);
        additionalDamage *= (float) attackCooldown;

        float knockback = (float) attacker.getAttributeValue(Attribute.ATTACK_KNOCKBACK);
        if (item.isPresent())
            knockback = item.get().getBonusKnockback(attacker, target, knockback, damageSource, stack);

        MeleeAttackEvent meleeEvent = new MeleeAttackEvent(attacker, target, damageSource, baseDamage + additionalDamage,
                knockback, strongAttack, sprintingAttack, criticalAttack, sweepingAttack);

        EventDispatcher.call(meleeEvent);
        if (meleeEvent.isCancelled()) {
            attackFail(instance, attacker);
            return;
        }

        performMeleeAttack(meleeEvent, item.orElse(null), engine);
    }

    // TODO: Add Magical damages.
    private static void performMeleeAttack(MeleeAttackEvent event, @Nullable CardinalItem item, GameFightEngine engine) {
        boolean strongAttack = event.isStrongAttack();
        boolean sprintingAttack = event.isSprintAttack();
        boolean criticalAttack = event.isCriticalAttack();
        boolean sweepAttack = event.isSweepAttack();

        float knockback = event.getKnockback();
        float damage = event.getDamage();

        Damage damageSource = event.getDamageSource();
        Instance instance = event.getInstance();

        LivingEntity attacker = event.getAttacker();
        LivingEntity target = event.getTarget();

        if (item != null && !item.onAttack(attacker, target, damageSource, attacker.getItemInMainHand())) {
            attackFail(instance, attacker);
            return;
        }

        if (criticalAttack)
            damage = engine.criticalDamageModifier(damage);

        if (sprintingAttack)
            knockback++;

        float targetHealth = target.getHealth();

        damageSource.setAmount(damage);
        boolean damaged = target.damage(damageSource);
        if (!damaged) {
            attackFail(instance, attacker);
            return;
        }

        if (engine.cancelSprintingAfterAttack()) {
            // attacker.setVelocity(attacker.getVelocity().mul(0.6, 1.0, 0.6)); TODO: Properly handle velocity changes, doesn't match the vanilla code, server needs to lie to the client.
            attacker.setSprinting(false);
        }

        if (knockback > 0.0F) {
            double knockbackX = Math.sin(attacker.getPosition().yaw() * Math.PI / 180);
            double knockbackZ = -Math.cos(attacker.getPosition().yaw() * Math.PI / 180);
            target.takeKnockback(knockback * 0.5F, knockbackX, knockbackZ);
        }

        // TODO: Implement sweeping attack

        if (sprintingAttack) {
            instance.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_ATTACK_KNOCKBACK, retrieveSoundCategory(attacker), 1.0F, 1.0F), attacker.getPosition());
        }

        if (criticalAttack) {
            instance.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_ATTACK_CRIT, retrieveSoundCategory(attacker), 1.0F, 1.0F), attacker.getPosition());
            playCritEffect(attacker, target);
        }

        if (strongAttack) {
            instance.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_ATTACK_STRONG, retrieveSoundCategory(attacker), 1.0F, 1.0F), attacker.getPosition());
        } else {
            instance.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_ATTACK_WEAK, retrieveSoundCategory(attacker), 1.0F, 1.0F), attacker.getPosition());
        }

        float damageDone = targetHealth - target.getHealth();
        if (damageDone > 2) {
            int particleCount = (int) (damageDone / 2);
            Pos targetPosition = target.getPosition();
            target.sendPacketToViewersAndSelf(new ParticlePacket(
                    Particle.DAMAGE_INDICATOR, false, false,
                    targetPosition.x(), targetPosition.y() + target.getBoundingBox().height() * 0.5, targetPosition.z(),
                    0.1f, 0, 0.1f,
                    0.2F, particleCount
            ));
        }

        if (attacker instanceof CardinalPlayer player)
            player.addExhaustion(engine.attackExhaustion());
    }

    private static float getAmountDamageBlocked(LivingEntity entity, Damage damage) {
        if (damage.getAmount() <= 0.0F)
            return 0.0F;

        PlayerHand hand = getBlockingHand(entity);
        if (hand == null)
            return 0.0F;

        ItemStack blockingItem = entity.getItemInHand(hand);
        if (blockingItem.isAir())
            return 0.0F;

        CardinalItem item = CardinalItem.fromStack(blockingItem).orElse(null);
        if (item == null)
            return 0.0F;

        if (!item.canBlockDamage(damage, entity))
            return 0.0F;

        float damageBlocked = item.blockDamage(entity, hand, blockingItem, damage);

        EntityBlockDamageEvent event = new EntityBlockDamageEvent(entity, hand, blockingItem, damage, damageBlocked);
        EventDispatcher.call(event);
        if (event.isCancelled())
            return 0.0F;

        return event.damageBlocked();
    }

    public static void postDamageBlock(LivingEntity entity, Damage damage, float blockedDamage) {
        PlayerHand hand = getBlockingHand(entity);
        if (hand == null)
            return;

        ItemStack blockingItem = entity.getItemInHand(hand);
        if (blockingItem.isAir())
            return;

        CardinalItem item = CardinalItem.fromStack(blockingItem).orElse(null);
        if (item == null)
            return;

        item.onBlock(entity, hand, blockingItem, damage, blockedDamage);
    }

    private static PlayerHand getBlockingHand(LivingEntity entity) {
        LivingEntityMeta meta = entity.getLivingEntityMeta();
        if (meta == null)
            return null;

        if (!meta.isHandActive()) {
            return null;
        }

        return meta.getActiveHand();
    }

    // TODO: Check for additional conditions (Potion Effects, Climbing, Fall distance if higher than 0, isTouching fluids)
    private static boolean isCriticalAttack(LivingEntity attacker) {
        return !attacker.isOnGround() && attacker.getVehicle() == null && !attacker.isSprinting();
    }

    private static void playCritEffect(LivingEntity attacker, LivingEntity target) {
        attacker.sendPacketToViewersAndSelf(new EntityAnimationPacket(target.getEntityId(), EntityAnimationPacket.Animation.CRITICAL_EFFECT));
    }

    private static boolean isSweeping(LivingEntity attacker) {
        Vec velocity = attacker.getVelocity();
        double distance = MathUtils.square(velocity.x()) + MathUtils.square(velocity.z());
        double speed = attacker.getAttributeValue(Attribute.MOVEMENT_SPEED);

        return distance > MathUtils.square(speed) && attacker.getItemInMainHand().has(DataComponents.WEAPON);
    }

    private static void attackFail(Instance instance, Entity attacker) {
        instance.playSound(Sound.sound(SoundEvent.ENTITY_PLAYER_ATTACK_NODAMAGE, retrieveSoundCategory(attacker), 1.0F, 1.0F), attacker.getPosition());
    }

    private static Sound.Source retrieveSoundCategory(Entity entity) {
        return entity instanceof CardinalEntity cardinalEntity ? cardinalEntity.getSoundSource() : Sound.Source.NEUTRAL;
    }
}
