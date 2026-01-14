package fr.atlasworld.cardinal.api.command.argument;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.command.suggestion.RegistrySuggestionCallback;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Utility class used for arguments that use registries to retrieve values.
 *
 * @param <T> type of the argument and the registry.
 */
public class RegistryBasedArgument<T> extends Argument<@NotNull T> {
    public static final int INVALID_KEY_ERROR = 1;
    public static final int ELEMENT_NOT_FOUND_ERROR = 2;

    protected final Registry<T> registry;

    public RegistryBasedArgument(@NotNull String id, @NotNull Registry<T> registry) {
        super(id, false, false);

        Preconditions.checkNotNull(registry, "Registry cannot be null");
        this.registry = registry;

        this.setSuggestionCallback(new RegistrySuggestionCallback<>(this.registry));
    }

    @Override
    public @NotNull T parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        Optional<RegistryKey> key = RegistryKey.fromString(input);
        if (key.isEmpty())
            throw new ArgumentSyntaxException("Invalid registry key", input, INVALID_KEY_ERROR);

        Optional<T> element = this.registry.retrieveValue(key.get());
        if (element.isEmpty())
            throw new ArgumentSyntaxException("Element not found", input, ELEMENT_NOT_FOUND_ERROR);

        return element.get();
    }

    @Override
    public @NotNull ArgumentParserType parser() {
        return ArgumentParserType.RESOURCE_LOCATION;
    }

    /**
     * Retrieve the registry used by the argument.
     *
     * @return {@link Registry} used by the argument.
     */
    public Registry<T> getRegistry() {
        return registry;
    }

    /**
     * {@link RegistryBasedArgument} based onto Cardinal internal Game registry.
     *
     * @return {@link RegistryBasedArgument} based onto Cardinal internal Game registry.
     * @deprecated create a new RegistryBasedArgument with the {@link CardinalRegistries#GAMES} registry.
     */
    @Deprecated
    public static RegistryBasedArgument<Game> game(@NotNull String name) {
        return new RegistryBasedArgument<>(name, CardinalRegistries.GAMES);
    }

    /**
     * {@link RegistryBasedArgument} based onto Cardinal internal GameMap registry.
     *
     * @return {@link RegistryBasedArgument} based onto Cardinal internal GameMap registry.
     * @deprecated create a new RegistryBasedArgument with the {@link CardinalRegistries#MAPS} registry.
     */
    @Deprecated
    public static RegistryBasedArgument<GameMap> map(@NotNull String name) {
        return new RegistryBasedArgument<>(name, CardinalRegistries.MAPS);
    }

    /**
     * {@link RegistryBasedArgument} based onto Cardinal internal Item registry.
     *
     * @return {@link RegistryBasedArgument} based onto Cardinal internal Item registry.
     * @deprecated create a new RegistryBasedArgument with the {@link CardinalRegistries#ITEMS} registry.
     */
    @Deprecated
    public static RegistryBasedArgument<CardinalItem> item(@NotNull String name) {
        return new RegistryBasedArgument<>(name, CardinalRegistries.ITEMS);
    }

    /**
     * {@link RegistryBasedArgument} based onto Cardinal internal Enchantment registry.
     *
     * @return {@link RegistryBasedArgument} based onto Cardinal internal Enchantment registry.
     * @deprecated create a new RegistryBasedArgument with the {@link CardinalRegistries#ENCHANTMENTS} registry.
     */
    @Deprecated
    public static RegistryBasedArgument<CardinalEnchantment> enchantment(@NotNull String name) {
        return new RegistryBasedArgument<>(name, CardinalRegistries.ENCHANTMENTS);
    }
}
