package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.game.GameLogic;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class GameImpl implements Game {
    private final Component displayName;
    private final Supplier<GameLogic> logicSupplier;
    private final boolean canShareMap;
    private final Map<String, RegistryHolder<DimensionType>> dimensions;
    private final Plugin plugin;

    private GameImpl(Component displayName, Supplier<GameLogic> logicSupplier, boolean canShareMap, Map<String, RegistryHolder<DimensionType>> dimensions, @Nullable Plugin plugin) {
        this.displayName = displayName;
        this.logicSupplier = logicSupplier;
        this.canShareMap = canShareMap;
        this.dimensions = dimensions;
        this.plugin = plugin;
    }

    @Override
    public @NotNull Component displayName() {
        return this.displayName;
    }

    @Override
    public @NotNull GameContainer create(@NotNull GameMap map) {
        return CardinalServer.instance().gameManager().createGame(this, map);
    }

    @Override
    public boolean canShareMap() {
        return this.canShareMap;
    }

    @Override
    public @NotNull RegistryKey<@NotNull DimensionType> dimension(@NotNull String identifier) {
        return Objects.requireNonNull(MinecraftServer.getDimensionTypeRegistry().getKey(this.dimensions.get(identifier).get()));
    }

    public GameLogic supplyLogic() {
        return this.logicSupplier.get();
    }

    public @Nullable Plugin plugin() {
        return this.plugin;
    }

    public static final class BuilderImpl implements Game.Builder {
        private final Map<String, RegistryHolder<DimensionType>> dimensions;

        private Component displayName;
        private Supplier<GameLogic> logicSupplier;
        private boolean canShareMap;

        private Plugin plugin;

        public BuilderImpl() {
            this.canShareMap = false;
            this.dimensions = new HashMap<>();
        }

        @Override
        public @NotNull BuilderImpl displayName(@NotNull Component displayName) {
            Preconditions.checkNotNull(displayName, "Display name cannot be null");

            this.displayName = displayName;
            return this;
        }

        @Override
        public @NotNull BuilderImpl logic(@NotNull Supplier<GameLogic> supplier) {
            Preconditions.checkNotNull(supplier, "Logic supplier cannot be null");

            this.logicSupplier = supplier;
            return this;
        }

        @Override
        public @NotNull BuilderImpl canShareMap(boolean canShareMap) {
            this.canShareMap = canShareMap;
            return this;
        }

        @Override
        public @NotNull BuilderImpl dimension(@NotNull String dimension, @NotNull RegistryHolder<DimensionType> type) {
            Preconditions.checkNotNull(dimension, "Dimension key cannot be null");
            Preconditions.checkNotNull(type, "Dimension type cannot be null");

            if (this.dimensions.containsKey(dimension))
                throw new IllegalArgumentException("Dimension '" + dimension + "' already exists!");

            this.dimensions.put(dimension, type);
            return this;
        }

        public @NotNull BuilderImpl plugin(@NotNull Plugin plugin) {
            Preconditions.checkNotNull(plugin, "Plugin cannot be null");
            this.plugin = plugin;
            return this;
        }

        @Override
        public @NotNull Game build() {
            Preconditions.checkNotNull(this.displayName, "Display name must be defined!");
            Preconditions.checkNotNull(this.logicSupplier, "Logic supplier must be defined!");
            Preconditions.checkArgument(!this.dimensions.isEmpty(), "At least one dimension must be defined!");
            Preconditions.checkNotNull(this.plugin, "Plugin must be defined!");

            return new GameImpl(this.displayName, this.logicSupplier, this.canShareMap, this.dimensions, this.plugin);
        }

        public @NotNull Game buildWithoutGame() {
            Preconditions.checkNotNull(this.displayName, "Display name must be defined!");
            Preconditions.checkNotNull(this.logicSupplier, "Logic supplier must be defined!");
            Preconditions.checkArgument(!this.dimensions.isEmpty(), "At least one dimension must be defined!");

            return new GameImpl(this.displayName, this.logicSupplier, this.canShareMap, this.dimensions, null);
        }
    }
}
