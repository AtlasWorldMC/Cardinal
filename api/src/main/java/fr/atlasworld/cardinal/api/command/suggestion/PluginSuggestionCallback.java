package fr.atlasworld.cardinal.api.command.suggestion;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;

public class PluginSuggestionCallback implements SuggestionCallback {
    private static final PluginSuggestionCallback INSTANCE = new PluginSuggestionCallback();

    private PluginSuggestionCallback() {
    }

    public static PluginSuggestionCallback create() {
        return INSTANCE;
    }

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion) {
        for (Plugin plugin : CardinalServer.getServer().pluginManager().loadedPlugins()) {
            suggestion.addEntry(new SuggestionEntry(plugin.namespace()));
        }
    }
}
