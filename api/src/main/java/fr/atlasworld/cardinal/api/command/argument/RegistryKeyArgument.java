package fr.atlasworld.cardinal.api.command.argument;

import fr.atlasworld.cardinal.api.registry.RegistryKey;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Registry key argument.
 */
public class RegistryKeyArgument extends Argument<@NotNull RegistryKey> {
    public static final int INVALID_KEY_ERROR = 1;

    public RegistryKeyArgument(@NotNull String id) {
        super(id, false, false);
    }

    @Override
    public @NotNull RegistryKey parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        Optional<RegistryKey> key = RegistryKey.fromString(input);
        if (key.isEmpty())
            throw new ArgumentSyntaxException("Invalid registry key", input, INVALID_KEY_ERROR);

        return key.get();
    }

    @Override
    public @NotNull ArgumentParserType parser() {
        return ArgumentParserType.RESOURCE_LOCATION;
    }
}
