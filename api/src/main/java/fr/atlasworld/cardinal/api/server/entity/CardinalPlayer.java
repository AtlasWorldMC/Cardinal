package fr.atlasworld.cardinal.api.server.entity;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import fr.atlasworld.cardinal.api.event.player.PlayerExhaustEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.server.CardinalGameRules;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Abstract player implementation with Cardinal-specific features.
 */
public abstract class CardinalPlayer extends Player implements CardinalEntity {

    @ApiStatus.Internal
    protected CardinalPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    @Override
    public @NotNull Sound.Source getSoundSource() {
        return Sound.Source.PLAYER;
    }

    // Game system

    /**
     * Retrieve the {@link GameContainer} in which the player is currently in.
     *
     * @return optional containing the container, or an empty optional if the player isn't currently in a container.
     */
    public abstract Optional<GameContainer> gameContainer();

    /**
     * Retrieve whether the player is a game spectator.
     *
     * @return {@code true} if the player is a game spectator, {@code false} otherwise.
     */
    public abstract boolean isGameSpectator();

    /**
     * Clears and resets all player states.
     * <br>
     * This will close any open screens (Inventory, Dialog, ...), reset the player attributes,
     * clear the inventory and cancel any using items, etc...
     * <p>
     * <b>Warning:</b> Scoreboard / Sidebars won't be reset with this method, you still need to unregister the player from them.
     * GameModes also aren't affected.
     * <br>
     * This won't clear the player's current game state (In which game they are or if they're a spectator)
     */
    public abstract void resetPlayerState();

    // GamePlay related fields.

    /**
     * Whether the player is currently hungry.
     *
     * @return {@code true} if the player is hungry, {@code false} otherwise.
     */
    public boolean hungry() {
        return this.getFood() < 20;
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
     * Checks whether the player is hungry.
     *
     * @return {@code true} if the player can eat food, {@code false} if not.
     */
    public boolean isHungry() {
        return this.getFood() < 20;
    }

    /**
     * Retrieve the last time the player attacked another entity.
     * <br>
     * This gives the tick timestamp of the last attack.
     *
     * @return the tick timestamp of the last attack.
     */
    public abstract long lastAttackTime();

    /**
     * Reset the cooldown between two attacks.
     * <br>
     * This will make that the player's attack cool down to its current {@link Entity#getAliveTicks()} value.
     * And this will make the player have to wait again to attack.
     */
    public abstract void resetAttackCooldown();

    /**
     * Retrieve the exhaustion level of the player.
     *
     * @return exhaustion level of the player.
     */
    public abstract float exhaustionLevel();

    /**
     * Sets the player's current exhaustion level.
     * <br>
     * <b>Note:</b> the exhaustion of a player cannot be higher than {@code 40.0F}
     * <br>
     * This will not call the {@link PlayerExhaustEvent}.
     *
     * @param exhaustionLevel player exhaustion level.
     */
    public abstract void setExhaustionLevel(float exhaustionLevel);

    /**
     * Add exhaustion to the player's current exhaustion level.
     * <br>
     * This will also call the {@link PlayerExhaustEvent}.
     *
     * @param exhaustion exhaustion to be set.
     *
     * @return the new exhaustion level.
     */
    @CanIgnoreReturnValue
    public float addExhaustion(float exhaustion) {
        if (exhaustion == 0 || this.getGameMode().invulnerable())
            return this.exhaustionLevel();

        if (this.gameContainer().isEmpty() || !this.gameContainer().get().getRuleValueOrDefault(CardinalGameRules.HUNGER.get(), false))
            return this.exhaustionLevel();

        PlayerExhaustEvent event = new PlayerExhaustEvent(this, exhaustion);
        EventDispatcher.call(event);

        if (event.isCancelled())
            return this.exhaustionLevel();

        this.setExhaustionLevel(this.exhaustionLevel() + event.exhaustion());
        return this.exhaustionLevel();
    }
}
