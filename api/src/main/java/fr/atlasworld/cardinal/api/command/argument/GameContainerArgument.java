package fr.atlasworld.cardinal.api.command.argument;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.game.GameContainer;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Game container argument, used to determine which game container a sender targets.
 */
public class GameContainerArgument extends Argument<@NotNull GameContainer> {
    public static final int CONTAINER_NOT_FOUND = 2;

    public GameContainerArgument(@NotNull String id) {
        super(id, false, false);
    }

    @Override
    public @NotNull GameContainer parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        for (GameContainer container : CardinalServer.getServer().gameManager().activeGames()) {
            if (container.displayName().equals(input))
                return container;
        }

        throw new ArgumentSyntaxException("Game container not found.", input, CONTAINER_NOT_FOUND);
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.STRING;
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, 0);
    }
}
