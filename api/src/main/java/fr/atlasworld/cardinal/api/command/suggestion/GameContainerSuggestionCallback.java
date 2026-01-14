package fr.atlasworld.cardinal.api.command.suggestion;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.game.GameContainer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Game container suggestion callback.
 */
public class GameContainerSuggestionCallback implements SuggestionCallback {

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion) {
        for (GameContainer container : CardinalServer.getServer().gameManager().activeGames()) {
            suggestion.addEntry(new SuggestionEntry(container.displayName()));
        }
    }
}
