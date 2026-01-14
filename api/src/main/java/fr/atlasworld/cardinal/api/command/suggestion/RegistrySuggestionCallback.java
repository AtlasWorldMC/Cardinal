package fr.atlasworld.cardinal.api.command.suggestion;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.kyori.adventure.key.Key;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of a {@link SuggestionCallback} to suggest entries in registries.
 *
 * @param <T> type of the registry.
 */
public class RegistrySuggestionCallback<T> implements SuggestionCallback {
    protected final Registry<T> registry;
    protected final @NotNull Filter<T> filter;

    public RegistrySuggestionCallback(@NotNull Registry<T> registry, @Nullable Filter<T> filter) {
        Preconditions.checkNotNull(registry, "Registry cannot be null!");
        this.registry = registry;
        this.filter = filter == null ? ((key, value, ctx) -> true) : filter;
    }

    public RegistrySuggestionCallback(@NotNull Registry<T> registry) {
        this(registry, null);
    }

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion) {
        this.registry.entries().forEach(entry -> {
            if (!this.filter.filter(entry.getKey(), entry.getValue(), context))
                return;

            suggestion.addEntry(new SuggestionEntry(entry.getKey().asString()));
        });
    }

    /**
     * Create a game suggestion callback.
     *
     * @return game suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#GAMES} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<Game> game() {
        return new RegistrySuggestionCallback<>(CardinalRegistries.GAMES);
    }

    /**
     * Create a game suggestion callback with a filter.
     *
     * @param filter predicate used to filter out elements in the suggestion.
     * @return game suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#GAMES} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<Game> game(@Nullable Filter<Game> filter) {
        return new RegistrySuggestionCallback<>(CardinalRegistries.GAMES, filter);
    }

    /**
     * Create a map suggestion callback.
     *
     * @return map suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#MAPS} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<GameMap> map() {
        return new RegistrySuggestionCallback<>(CardinalRegistries.MAPS);
    }

    /**
     * Create a map suggestion callback with a filter.
     *
     * @param filter predicate used to filter out elements in the suggestion.
     * @return game suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#MAPS} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<GameMap> map(@Nullable Filter<GameMap> filter) {
        return new RegistrySuggestionCallback<>(CardinalRegistries.MAPS, filter);
    }

    /**
     * Create an item suggestion callback.
     *
     * @return item suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#ITEMS} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<CardinalItem> item() {
        return new RegistrySuggestionCallback<>(CardinalRegistries.ITEMS);
    }

    /**
     * Create an item suggestion callback with a filter.
     *
     * @param filter predicate used to filter out elements in the suggestion.
     * @return item suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#ITEMS} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<CardinalItem> item(@Nullable Filter<CardinalItem> filter) {
        return new RegistrySuggestionCallback<>(CardinalRegistries.ITEMS, filter);
    }

    /**
     * Create an enchantment suggestion callback.
     *
     * @return enchantment suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#ENCHANTMENTS} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<CardinalEnchantment> enchantment() {
        return new RegistrySuggestionCallback<>(CardinalRegistries.ENCHANTMENTS);
    }

    /**
     * Create an enchantment suggestion callback with a filter.
     *
     * @param filter predicate used to filter out elements in the suggestion.
     * @return enchantment suggestion callback.
     * @deprecated create a new instance of {@link RegistrySuggestionCallback} with the {@link CardinalRegistries#ENCHANTMENTS} registry.
     */
    @Deprecated
    public static RegistrySuggestionCallback<CardinalEnchantment> enchantment(@Nullable Filter<CardinalEnchantment> filter) {
        return new RegistrySuggestionCallback<>(CardinalRegistries.ENCHANTMENTS, filter);
    }

    @FunctionalInterface
    public interface Filter<T> {

        /**
         * Filter an element from the suggestion callback.
         *
         * @param key   key of the entry.
         * @param value value of the entry.
         * @param ctx   command context.
         * @return {@code true} if the element should be kept, {@code false} otherwise.
         */
        boolean filter(@NotNull Key key, @NotNull T value, @NotNull CommandContext ctx);
    }
}
