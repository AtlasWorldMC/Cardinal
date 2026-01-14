package fr.atlasworld.cardinal.api.command.condition;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Command condition implementation used to check if a player is in a specific game.
 */
public final class GameCondition implements CommandCondition {
    private final Game game;
    private final boolean strict;

    private GameCondition(@NotNull Game game, boolean strict) {
        Preconditions.checkNotNull(game, "Game cannot be null!");
        this.game = game;
        this.strict = strict;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        if (!(sender instanceof CardinalPlayer player))
            return false;

        Optional<GameContainer> container = player.gameContainer();
        if (container.isEmpty())
            return false;

        if (!this.strict && container.get().game() == this.game)
            return true;

        Instance instance = player.getInstance();
        if (instance == null)
            return false;

        return container.get().isInstanceLinked(instance) && this.game == container.get().game();
    }

    /**
     * Create a game condition of a specific game.
     *
     * @param game game to check for.
     * @return newly created GameCondition.
     */
    public static @NotNull GameCondition of(@NotNull Game game) {
        return new GameCondition(game, true);
    }

    /**
     * Create a game condition of a specific game.
     *
     * @param game   game to check for.
     * @param strict whether the check should be strict, setting this to {@code true} will double-check that the player
     *               is inside an instance linked to the game.
     * @return newly created GameCondition.
     */
    public static @NotNull GameCondition of(@NotNull Game game, boolean strict) {
        return new GameCondition(game, strict);
    }
}
