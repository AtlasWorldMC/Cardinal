package fr.atlasworld.cardinal.api.game.builtin;

import fr.atlasworld.cardinal.api.event.player.PlayerJoinGameEvent;
import fr.atlasworld.cardinal.api.game.GameContext;
import fr.atlasworld.cardinal.api.game.GameLogic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TestEnvLogic implements GameLogic {
    public static final Component PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("\uD83E\uDEB2", NamedTextColor.GREEN))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY));

    private static final Component WELCOME_MESSAGE = Component.text("Welcome to the test environment!", NamedTextColor.GOLD)
            .appendNewline().append(Component.text("In this environment you're free to use and test any features without needing to worry about breaking other games.", NamedTextColor.WHITE))
            .appendNewline().appendNewline().append(Component.text("A special command is available to change or add features to this environment.", NamedTextColor.WHITE))
            .appendNewline().append(Component.text("/testenv", NamedTextColor.GOLD, TextDecoration.UNDERLINED));

    public static final String PRIMARY_DIM_IDENTIFIER = "primary";
    public static final String SECONDARY_DIM_IDENTIFIER = "secondary";
    public static final Pos SPAWNPOINT = new Pos(0.5, 5, 0.5);

    private final Entity welcomeMessage;

    public TestEnvLogic() {
        this.welcomeMessage = new Entity(EntityType.TEXT_DISPLAY);
        TextDisplayMeta meta = (TextDisplayMeta) this.welcomeMessage.getEntityMeta();
        meta.setText(WELCOME_MESSAGE);
        meta.setAlignment(TextDisplayMeta.Alignment.CENTER);
        meta.setHasNoGravity(true);
    }

    @Override
    public void initialize(@NotNull GameContext ctx) {
        Instance mainInstance = ctx.createInstance(PRIMARY_DIM_IDENTIFIER);

        this.welcomeMessage.setInstance(mainInstance, new Pos(0.5, 6, 7));
        this.welcomeMessage.lookAt(SPAWNPOINT);

        ctx.setJoiningInstance(mainInstance);
        ctx.allowPlayerJoining(true);

        ctx.eventNode().addListener(PlayerJoinGameEvent.class, this::handlePlayerJoin);
        ctx.eventNode().addListener(PlayerMoveEvent.class, this::handlePlayerMove);
        ctx.eventNode().addListener(PlayerGameModeChangeEvent.class, this::handlePlayerGameModeSwitch);
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
        this.welcomeMessage.remove();
    }

    private void handlePlayerJoin(PlayerJoinGameEvent event) {
        Player player = event.getPlayer();

        player.setGameMode(GameMode.CREATIVE);
        player.setPermissionLevel(4);

        player.teleport(SPAWNPOINT);
        player.setRespawnPoint(SPAWNPOINT);
    }

    private void handlePlayerGameModeSwitch(PlayerGameModeChangeEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        GameMode currentGamemode = player.getGameMode();
        GameMode requestedGamemode = event.getNewGameMode();

        if (currentGamemode == requestedGamemode)
            return;

        player.sendMessage(PREFIX.append(Component.text("Game mode switched to ", NamedTextColor.GRAY)
                .append(Component.text(requestedGamemode.name().toLowerCase(), NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.GRAY))));
    }

    private void handlePlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Instance instance = player.getInstance();
        Pos position = event.getNewPosition();

        if (player.isFlying())
            return;

        boolean isInVoid = instance.isInVoid(position);
        if (!isInVoid)
            return;

        player.teleport(SPAWNPOINT);
    }
}
