package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.game.*;
import fr.atlasworld.cardinal.server.entity.CardinalPlayerImpl;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class GameContextImpl implements GameContext {
    private final GameContainerImpl container;
    private final EventNode<@NotNull Event> eventNode;

    private volatile Instance joiningInstance;
    private volatile boolean allowPlayerJoin;

    public GameContextImpl(GameContainerImpl container, EventNode<@NotNull Event> eventNode) {
        this.container = container;
        this.eventNode = eventNode;
        this.allowPlayerJoin = true;
    }

    @Override
    public @NotNull GameContainer container() {
        return this.container;
    }

    @Override
    public @NotNull GameMap map() {
        return this.container.map();
    }

    @Override
    public @NotNull EventNode<@NotNull Event> eventNode() {
        return this.eventNode;
    }

    @Override
    public @NotNull Instance createInstance(GameMap.@NotNull MapWorld world) {
        if (!this.validateMapWorld(world))
            throw new IllegalArgumentException("World is not part of the map!");

        return this.container.createInstance(world);
    }

    @Override
    public @NotNull Instance createInstance(@NotNull String dimensionKey) {
        for (GameMap.MapWorld world : this.map().worlds()) {
            if (world.dimension().equals(dimensionKey))
                return this.container.createInstance(world);
        }

        throw new IllegalArgumentException("Dimension key is not part of the map!");
    }

    private boolean validateMapWorld(GameMap.MapWorld world) {
        for (GameMap.MapWorld map : this.map().worlds()) {
            if (map.equals(world))
                return true;
        }

        return false;
    }

    @Override
    public Optional<Instance> retrieveInstance(@NotNull UUID identifier) {
        return this.container.retrieveInstance(identifier);
    }

    @Override
    public Set<Instance> retrieveInstances() {
        return this.container.instances();
    }

    @Override
    public @NotNull Set<Player> retrievePlayers() {
        Set<Player> players = new HashSet<>();

        this.retrieveInstances().forEach(instance -> {
            for (Player player : instance.getPlayers()) {
                CardinalPlayerImpl cardinalPlayer = (CardinalPlayerImpl) player;
                if (!cardinalPlayer.isGameSpectator())
                    players.add(player);
            }
        });

        return players;
    }

    @Override
    public void setJoiningInstance(@NotNull Instance instance) {
        Preconditions.checkNotNull(instance, "Instance cannot be null");
        this.joiningInstance = instance;
    }

    @Override
    public void allowPlayerJoining(boolean allowPlayerJoin) {
        this.allowPlayerJoin = allowPlayerJoin;
    }

    @Override
    public <T> void setDefaultRule(@NotNull GameRule<T> rule, @Nullable T value) {
        this.container.ruleStore().setGame(rule, value);
    }

    @Override
    public <T> @Nullable T getRuleValue(@NotNull GameRule<T> rule) {
        return this.container.getRuleValue(rule);
    }

    @Override
    public <T> @NotNull T getRuleValueOrDefault(@NotNull GameRule<T> rule, @NotNull T fallback) {
        return this.container.getRuleValueOrDefault(rule, fallback);
    }

    public boolean allowPlayerJoining() {
        return this.allowPlayerJoin;
    }

    public @Nullable Instance joiningInstance() {
        return this.joiningInstance;
    }
}
