package fr.atlasworld.cardinal.api.game.builtin;

import fr.atlasworld.cardinal.api.event.player.PlayerJoinGameEvent;
import fr.atlasworld.cardinal.api.game.GameContext;
import fr.atlasworld.cardinal.api.game.GameLogic;
import fr.atlasworld.cardinal.api.server.CardinalGameRules;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.WorldBorder;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Limbo world implementation.
 */
public final class LimboGameLogic implements GameLogic {
    private static final Sound JOIN_SFX = Sound.sound(Key.key("block.end_portal.spawn"), Sound.Source.AMBIENT, 0.2f, 0.12f);
    private static final Potion DARKNESS_EFFECT = new Potion(PotionEffect.DARKNESS, 3, 100);
    private static final Potion BLINDNESS_EFFECT = new Potion(PotionEffect.BLINDNESS, 3, 40);
    private static final Title WELCOME_TITLE = Title.title(Component.text("Limbo", NamedTextColor.GOLD),
            Component.text("A world between worlds", NamedTextColor.GRAY).decorate(TextDecoration.ITALIC));

    public static final String DIM_IDENTIFIER = "limbo";

    private Instance limboInstance;

    @Override
    public void initialize(@NotNull GameContext ctx) {
        ctx.setDefaultRule(CardinalGameRules.CAN_STARVE.get(), false);
        ctx.setDefaultRule(CardinalGameRules.HUNGER.get(), false);

        this.limboInstance = ctx.createInstance(DIM_IDENTIFIER);
        this.limboInstance.setWorldBorder(new WorldBorder(256, 0, 0, 0, 0));

        ctx.eventNode().addListener(PlayerJoinGameEvent.class, this::handlePlayerJoin);
        ctx.eventNode().addListener(PlayerMoveEvent.class, this::handlePlayerMove);
        ctx.eventNode().addListener(EntityDamageEvent.class, this::handleEntityDamage);
        ctx.eventNode().addListener(PlayerGameModeChangeEvent.class, this::handleGameModeSwitch);

        ctx.setJoiningInstance(this.limboInstance);
        ctx.allowPlayerJoining(true);
    }

    @Override
    public boolean update(@NotNull GameContext ctx) {
        return false;
    }

    @Override
    public void finish(@NotNull GameContext ctx) {

    }

    @Override
    public void interrupt(@NotNull GameContext ctx, @Nullable Throwable cause) {

    }

    @Override
    public void terminate(@NotNull GameContext ctx) {

    }

    private void handlePlayerJoin(PlayerJoinGameEvent event) {
        Player player = event.getPlayer();

        player.setGameMode(GameMode.ADVENTURE);
        player.addEffect(DARKNESS_EFFECT);
        player.showTitle(WELCOME_TITLE);
        player.playSound(JOIN_SFX, Sound.Emitter.self());
    }

    private void handleEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true); // Cancel any damage.
    }

    private void handleGameModeSwitch(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() != GameMode.ADVENTURE)
            event.setCancelled(true); // Prevent changing game mode.
    }

    private void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Pos position = event.getNewPosition();

        if (player.isFlying())
            return;

        boolean isInVoid = this.limboInstance.isInVoid(position);
        if (!isInVoid)
            return;

        player.addEffect(BLINDNESS_EFFECT);
        player.teleport(Pos.ZERO);
    }
}
