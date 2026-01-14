package fr.atlasworld.cardinal.api.command.argument;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PluginArgument extends Argument<@NotNull Plugin> {
    public static final int INVALID_IDENTIFIER_ERROR = 1;
    public static final int PLUGIN_NOT_FOUND_ERROR = 2;

    public PluginArgument(@NotNull String id) {
        super(id, false, false);
    }

    @Override
    public @NotNull Plugin parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        if (!RegistryKey.isValidNamespace(input))
            throw new ArgumentSyntaxException("Invalid identifier", input, INVALID_IDENTIFIER_ERROR);

        Optional<Plugin> plugin = CardinalServer.getServer().pluginManager().retrievePlugin(input);
        if (plugin.isEmpty())
            throw new ArgumentSyntaxException("Plugin not found", input, PLUGIN_NOT_FOUND_ERROR);

        return plugin.get();
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
