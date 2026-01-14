package fr.atlasworld.cardinal.server.entity;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.player.PlayerJoinGameEvent;
import fr.atlasworld.cardinal.api.event.player.PlayerSpectateGameEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.server.CardinalGameRules;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import fr.atlasworld.cardinal.api.server.item.ItemCooldownManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityPose;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.entity.metadata.PlayerMeta;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class CardinalPlayerImpl extends CardinalPlayer {
    private final ItemCooldownManager itemCooldownManager;

    // Game related data
    private volatile @UnknownNullability GameContainer gameContainer;
    private volatile boolean notifyContainerChange; // Used to determine whether to call events or not.
    private volatile boolean gameSpectator;

    // Gameplay
    private long lastAttackTime = 0;

    private float exhaustionLevel = 0.0F;
    private int foodTick = 0;

    public CardinalPlayerImpl(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);

        this.itemCooldownManager = new ItemCooldownManager(this);

        this.gameContainer = null;
        this.notifyContainerChange = false;
        this.gameSpectator = false;

        this.initializeAttributes();
    }

    private void initializeAttributes() {
        this.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(1);
        this.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
        this.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).setBaseValue(4.5);
        this.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).setBaseValue(3);
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        this.tickFood();
        this.tickRegeneration();
    }

    @Override
    public Optional<GameContainer> gameContainer() {
        return Optional.ofNullable(this.gameContainer);
    }

    @Override
    public boolean isGameSpectator() {
        return this.gameSpectator;
    }

    public void setGame(@NotNull GameContainer gameContainer, boolean gameSpectator) {
        Preconditions.checkNotNull(gameContainer, "Game container cannot be null!");

        if (this.gameContainer != gameContainer)
            this.notifyContainerChange = true; // Will call the events once the player is loaded in the world.

        boolean notifyModeChange = this.gameSpectator != gameSpectator && !this.notifyContainerChange;

        this.gameContainer = gameContainer;
        this.gameSpectator = gameSpectator;

        if (notifyModeChange)
            if (this.gameSpectator)
                EventDispatcher.call(new PlayerSpectateGameEvent(this.gameContainer, this));
            else
                EventDispatcher.call(new PlayerJoinGameEvent(this.gameContainer, this));
    }

    @Override
    public @NotNull CompletableFuture<Void> setInstance(@NotNull Instance instance, @NotNull Pos spawnPosition) {
        Preconditions.checkNotNull(instance, "Instance cannot be null!");
        Preconditions.checkNotNull(this.gameContainer, "Instance is not part of the game the player is currently in.");
        Preconditions.checkArgument(this.gameContainer.isInstanceLinked(instance), "Instance is not part of the game the player is currently in.");

        return super.setInstance(instance, spawnPosition).thenAccept(unused -> {
            if (!this.notifyContainerChange)
                return;

            this.notifyContainerChange = false;
            this.refreshCommands();

            if (this.gameSpectator)
                EventDispatcher.call(new PlayerSpectateGameEvent(this.gameContainer, this));
            else
                EventDispatcher.call(new PlayerJoinGameEvent(this.gameContainer, this));
        });
    }

    @Override
    public long lastAttackTime() {
        return this.lastAttackTime;
    }

    @Override
    public void resetAttackCooldown() {
        this.lastAttackTime = this.getAliveTicks();
    }

    @Override
    public void respawn() {
        if (!this.isDead())
            return;

        super.respawn();
        this.exhaustionLevel = 0F;
    }

    // Food
    public void tickFood() {
        if (this.isDead())
            return;

        this.foodTick++;
        if (this.gameContainer == null || !this.gameContainer.getRuleValueOrDefault(CardinalGameRules.HUNGER.get(), false))
            return;

        if (this.exhaustionLevel > 4.0F) {
            this.addExhaustion(-4.0F);
            if (this.getFoodSaturation() > 0.0F) {
                this.setFoodSaturation(Math.max(this.getFoodSaturation() - 1.0F, 0.0F));
                return;
            }

            this.setFood(Math.max(this.getFood() - 1, 0));
        }

        if (this.getFood() <= 0 && this.foodTick >= 80) {
            boolean canStarve = this.gameContainer.getRuleValueOrDefault(CardinalGameRules.CAN_STARVE.get(), false);
            if (canStarve || this.getHealth() > 1.0F)
                this.damage(new Damage(DamageType.STARVE, null, null, null, 1.0F));
        }
    }

    @Override
    public float exhaustionLevel() {
        return this.exhaustionLevel;
    }

    @Override
    public void setExhaustionLevel(float exhaustionLevel) {
        this.exhaustionLevel = Math.min(exhaustionLevel, 40F);
    }

    // Healing
    public void tickRegeneration() {
        if (this.gameContainer == null || !this.gameContainer.getRuleValueOrDefault(CardinalGameRules.NATURAL_REGENERATION.get(), false) || this.isDead())
            return;

        if (this.getFoodSaturation() > 0.0F && this.isHurt() && this.getFood() >= 20) {
            if (this.foodTick < 10)
                return;

            float healingRatio = Math.min(this.getFoodSaturation(), 0.6F);
            this.setHealth(this.getHealth() + (healingRatio / 0.6F));
            this.addExhaustion(healingRatio);
            this.foodTick = 0;
            return;
        }

        if (this.getFood() >= 18 && this.isHurt()) {
            if (this.foodTick < 80)
                return;

            this.setHealth(this.getHealth() + 1.0F);
            this.addExhaustion(6.0F);
            this.foodTick = 0;
            return;
        }

        this.foodTick = 0;
    }

    @Override
    public @NotNull SoundEvent getHurtSound() {
        return SoundEvent.ENTITY_PLAYER_HURT;
    }

    @Override
    public void refreshItemUse(@Nullable PlayerHand itemUseHand, long itemUseTimeTicks) {
        super.refreshItemUse(itemUseHand, itemUseTimeTicks);
        this.refreshActiveHand(itemUseTimeTicks > 0, itemUseHand == PlayerHand.OFF, false);
    }

    @Override
    public @NotNull ItemCooldownManager cooldownManager() {
        return this.itemCooldownManager;
    }

    @Override
    public void resetPlayerState() {
        this.clearTitle();
        this.clearEffects();
        this.closeDialog();
        this.closeInventory();

        this.setPermissionLevel(1);

        this.getInventory().clear();
        this.clearItemUse();

        this.setExp(0F);
        this.setLevel(0);
        this.refreshHealth();
        this.setExhaustionLevel(0);

        this.setFireTicks(0);

        this.getAttributes().forEach(AttributeInstance::clearModifiers); // Remove any modifications on the attributes
        this.initializeAttributes();

        this.setInstantBreak(false);
        this.setFlying(false);
        this.setFlyingSpeed(0.05F);
        this.setAllowFlying(false);
        this.setInvulnerable(false);

        this.setBelowNameTag(null);
        this.setTeam(null);

        this.setDeathLocation(Pos.ZERO);
        this.setRespawnPoint(Pos.ZERO);

        // Meta
        PlayerMeta meta = this.getPlayerMeta();
        meta.setNotifyAboutChanges(false); // Disable the time we do all the changes to send only 1 packet.

        meta.setAdditionalHearts(0F);
        meta.setHasGlowingEffect(false);
        meta.setHasNoGravity(false);
        meta.setSilent(false);
        meta.setArrowCount(0);
        meta.setBeeStingerCount(0);
        meta.setFlyingWithElytra(false);
        meta.setInRiptideSpinAttack(false);
        meta.setPose(EntityPose.STANDING);
        meta.setInvisible(false);
        meta.setOnFire(false);
        meta.setSwimming(false);

        meta.setNotifyAboutChanges(true); // Send all changes
    }
}
