package fr.atlasworld.cardinal.api.game;

import fr.atlasworld.cardinal.api.game.GameContainer.State;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Game context holds all instance elements of a currently running game.
 * <p>
 * The Game context should not be shared across multiple games! It's unique to the game it's attached to.
 */
public interface GameContext {

    /**
     * Retrieve the container the game is running from.
     *
     * @return game container.
     */
    @NotNull GameContainer container();

    /**
     * Retrieve the game map currently used by the game.
     *
     * @return game map currently used by the game.
     */
    @NotNull GameMap map();

    /**
     * Retrieve the game-specific event node, event passed through this node a specific to this game.
     * <p>
     * You usually can use these events without any check beforehand,
     * the node filters out any events that aren't related to any instances of this container.
     * But if the event trait is unsupported, this won't filter out these events if they happen in another instance.
     * <br>
     * If the event you use extends any of the listed ones, it should filter them out.
     *
     * <p>
     * Supported events are:
     * <ul>
     *     <li>{@link net.minestom.server.event.trait.InstanceEvent}</li>
     *     <li>{@link net.minestom.server.event.trait.EntityEvent}</li>
     * </ul>
     *
     * @return event node.
     */
    @NotNull EventNode<@NotNull Event> eventNode();

    /**
     * Create a new instance.
     *
     * @param world map world to use.
     * @return newly created instance.
     * @throws IllegalArgumentException if the {@code world} is not part of the map being used by the game.
     * @throws IllegalStateException    if the instance could not be created.
     */
    @NotNull Instance createInstance(@NotNull GameMap.MapWorld world);

    /**
     * Create a new instance.
     *
     * @param dimensionKey map world key.
     * @return newly created instance.
     * @throws IllegalArgumentException if no world could be found with the specified key.
     * @throws IllegalStateException    if the instance could not be created.
     */
    @NotNull Instance createInstance(@NotNull String dimensionKey);

    /**
     * Retrieve an instance from its identifier.
     *
     * @param identifier identifier of the instance.
     * @return optional containing the instance, or an empty optional if no instance could be found with that identifier.
     */
    Optional<Instance> retrieveInstance(@NotNull UUID identifier);

    /**
     * Retrieve all the instances currently available in the game.
     *
     * @return set containing all instances of the game.
     */
    Set<Instance> retrieveInstances();

    /**
     * Retrieves all currently playing players.
     * <p>
     * This doesn't include spectators.
     *
     * @return set containing all currently playing players.
     */
    @NotNull Set<Player> retrievePlayers();

    /**
     * Sets the instance in which joining players should be teleported in.
     *
     * @param instance instance in which the players should be teleported in.
     */
    void setJoiningInstance(@NotNull Instance instance);

    /**
     * Sets whether players can join the game in the current state.
     * <p>
     * This only works for players, not spectators.
     * By default, players can always join.
     * If your games want to lock the possibility for players to join after an event, this is the way to do it.
     * <p>
     * <b>Warning:</b> locking the game at the {@link State#INITIALIZING initializing state}
     * will prevent any players to join during the {@link State#WAITING waiting state} which waits for player to join,
     * if your game needs player to start, this will cause the game to softlock and never reach the {@link State#RUNNING running state}.
     *
     * @param allowPlayerJoin {@code true} if players can join, {@code false} if the players can't join.
     */
    void allowPlayerJoining(boolean allowPlayerJoin);

    /**
     * Sets a default value for a game rule.
     * <br><br>
     * This is mostly used as recommendations for the rules that should be applied for the game.
     * <br>
     * <b>Warning:</b> these can be overridden at any time by external sources.
     *
     * @param rule rule to which the default value should be applied.
     * @param value value to set.
     * @param <T> type of value of the rule.
     */
    <T> void setDefaultRule(@NotNull GameRule<T> rule, @Nullable T value);

    /**
     * Retrieve the game rule value for the specified rule.
     *
     * @param rule rule to retrieve.
     *
     * @return the value set, or the default value if no value was set, if no rule was set and the game rule doesn't have a default value, {@code null} is returned.
     * @param <T> type of the game rule.
     */
    <T> @Nullable T getRuleValue(@NotNull GameRule<T> rule);

    /**
     * Retrieve the game rule value for the specified rule or fallback on a default value.
     *
     * @param rule rule to retrieve.
     * @param fallback fallback value if no value was set.
     *
     * @return the value set, or the default value if no value was set, if no rule was set and the game rule doesn't have a default value,
     *         the {@code fallback} value is returned.
     * @param <T> type of the game rule.
     */
    <T> @NotNull T getRuleValueOrDefault(@NotNull GameRule<T> rule, @NotNull T fallback);
}
