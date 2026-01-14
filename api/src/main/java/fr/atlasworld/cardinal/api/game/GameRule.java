package fr.atlasworld.cardinal.api.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.command.argument.RegistryBasedArgument;
import fr.atlasworld.cardinal.api.registry.Registry;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Game Rule used to change settings about the game.
 *
 * @param <T> type of value contained in the game rule.
 */
public final class GameRule<T> {
    private final Argument<@NotNull T> argument;
    private final @Nullable T defaultValue;

    /**
     * Create a new game rule.
     *
     * @param argument the command argument used to parse the value.
     * @param defaultValue the default value.
     */
    public GameRule(@NotNull Argument<@NotNull T> argument, @Nullable T defaultValue) {
        Preconditions.checkNotNull(argument, "Argument must not be null");

        this.argument = argument;
        this.defaultValue = defaultValue;
    }

    /**
     * Create a new game rule.
     *
     * @param argument the command argument used to parse the value.
     */
    public GameRule(@NotNull Argument<@NotNull T> argument) {
        this(argument, null);
    }

    /**
     * Retrieve the command argument.
     *
     * @return the command argument.
     */
    public @NotNull Argument<@NotNull T> argument() {
        return this.argument;
    }

    /**
     * The default value of the game rule.
     *
     * @return the default value, or {@code null} if no default value was defined.
     */
    public @Nullable T defaultValue() {
        return this.defaultValue;
    }

    /**
     * Create a boolean type game rule.
     *
     * @return a new boolean game rule.
     */
    public static GameRule<Boolean> ofBoolean() {
        return new GameRule<>(ArgumentType.Boolean("value"), null);
    }

    /**
     * Create a boolean type game rule.
     *
     * @param defaultValue the default value to use.
     *
     * @return a new boolean game rule.
     */
    public static GameRule<Boolean> ofBoolean(boolean defaultValue) {
        return new GameRule<>(ArgumentType.Boolean("value"), defaultValue);
    }

    /**
     * Create an integer type game rule.
     *
     * @return a new integer type game rule.
     */
    public static GameRule<Integer> ofInteger() {
        return new GameRule<>(ArgumentType.Integer("value"));
    }

    /**
     * Create an integer type game rule.
     *
     * @param defaultValue default value of the game rule.
     *
     * @return a new integer type game rule.
     */
    public static GameRule<Integer> ofInteger(int defaultValue) {
        return new GameRule<>(ArgumentType.Integer("value"), defaultValue);
    }

    /**
     * Create a double type game rule.
     *
     * @return a new double type game rule.
     */
    public static GameRule<Double> ofDouble() {
        return new GameRule<>(ArgumentType.Double("value"));
    }

    /**
     * Create a double type game rule.
     *
     * @param defaultValue default value of the game rule.
     *
     * @return a new double type game rule.
     */
    public static GameRule<Double> ofDouble(double defaultValue) {
        return new GameRule<>(ArgumentType.Double("value"), defaultValue);
    }

    /**
     * Create a game rule based on a {@link Registry}.
     *
     * @param registry the registry to use.
     *
     * @return a new game rule based on the registry.
     * @param <T> type of the registry.
     */
    public static <T> GameRule<T> ofRegistry(@NotNull Registry<T> registry) {
        return new GameRule<>(new RegistryBasedArgument<>("value", registry));
    }

    /**
     * Create a game rule based on a {@link Registry}.
     *
     * @param registry the registry to use.
     * @param defaultValue the default value to use.
     *
     * @return a new game rule based on the registry.
     * @param <T> type of the registry.
     */
    public static <T> GameRule<T> ofRegistry(@NotNull Registry<T> registry, @Nullable T defaultValue) {
        return new GameRule<>(new RegistryBasedArgument<>("value", registry), defaultValue);
    }
}
