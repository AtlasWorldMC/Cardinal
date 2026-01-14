package fr.atlasworld.cardinal.game;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.server.world.GameWorld;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.SharedInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Submanager of the {@link fr.atlasworld.cardinal.api.game.GameManager} to handle world loading and creation.
 */
public class CardinalWorldManager {
    private static final Logger LOGGER = Logging.logger();

    private final Map<Instance, GameContainer> gameInstances;
    private final BiMap<GameWorld, InstanceContainer> shareableInstances;

    public CardinalWorldManager() {
        this.gameInstances = new ConcurrentHashMap<>();
        this.shareableInstances = HashBiMap.create();
    }

    /**
     * Load an instance for the given instance using the {@link fr.atlasworld.cardinal.api.game.GameMap.MapWorld} definition.
     * <br><br>
     * If the game for which the instance will be loaded allows for map sharing, it will create a new shared instance instead of direct access to the main instance.
     *
     * @param container container for which the instance will be loaded.
     * @param world     world definition for the instance.
     * @return newly created instance or null if the instance could not be created.
     */
    public @Nullable Instance loadInstance(@NotNull GameContainerImpl container, @NotNull GameMap.MapWorld world) {
        Preconditions.checkNotNull(container, "Container cannot be null");
        Preconditions.checkNotNull(world, "World cannot be null");

        Game game = container.game();
        Instance instance = game.canShareMap() ? this.loadSharedInstance(world, container) : this.loadSingleInstance(world, container);
        if (instance == null)
            return null;

        this.gameInstances.put(instance, container);
        container.registerInstance(instance);

        return instance;
    }

    /**
     * Unregister an instance from the manager.
     * <br><br>
     * This will also unregister the instance from the server, and thus won't be ticked anymore.
     *
     * @param instance instance to unregister.
     */
    public void unregisterInstance(@NotNull Instance instance) {
        MinecraftServer.getInstanceManager().unregisterInstance(instance);
        if (this.gameInstances.remove(instance) == null) {
            LOGGER.error("Un-managed instance was registered inside in a GameContainer: {}", instance);
            return;
        }

        if (!(instance instanceof SharedInstance sharedInstance))
            return;

        synchronized (this.shareableInstances) {
            InstanceContainer container = sharedInstance.getInstanceContainer();
            boolean referenced = this.shareableInstances.containsValue(container);
            if (!referenced) {
                LOGGER.error("Un-managed shared instance was registered inside in a GameContainer: {}", sharedInstance);
                return;
            }

            if (!container.getSharedInstances().isEmpty()) {
                LOGGER.trace("Clearing shared instance, source instance still has child instances used.");
                return;
            }

            LOGGER.debug("Source instance used for shared instance has no longer any child share instances, unregistering source instance: {}", container);
            MinecraftServer.getInstanceManager().unregisterInstance(container);
            this.shareableInstances.inverse().remove(container);
        }
    }

    private Instance loadSharedInstance(@NotNull GameMap.MapWorld world, @NotNull GameContainerImpl container) {
        synchronized (this.shareableInstances) {
            InstanceContainer instance = this.shareableInstances.computeIfAbsent(world.world().get(), unused -> this.loadSingleInstance(world, container));
            if (instance == null)
                return null;

            LOGGER.trace("Creating shared instance for world '{}' for game '{}'.", world.dimension(), Serializers.PLAIN_TEXT.serialize(container.game().displayName()));
            return MinecraftServer.getInstanceManager().createSharedInstance(instance);
        }
    }

    private InstanceContainer loadSingleInstance(@NotNull GameMap.MapWorld world, @NotNull GameContainerImpl container) {
        try {
            InstanceContainer instance = new InstanceContainer(UUID.randomUUID(), container.game().dimension(world.dimension()));
            instance.setChunkSupplier(LightingChunk::new); // Adds light computing chunks
            world.world().get().provide(instance, world.extraParams());

            MinecraftServer.getInstanceManager().registerInstance(instance);
            return instance;
        } catch (Throwable ex) {
            LOGGER.error("Failed to load instance for world '{}' from '{}'.", world.dimension(), Serializers.PLAIN_TEXT.serialize(container.map().name()), ex);
            return null;
        }
    }

    public Optional<GameContainer> gameContainer(@NotNull Instance instance) {
        Preconditions.checkNotNull(instance, "Instance cannot be null!");

        return Optional.ofNullable(this.gameInstances.get(instance));
    }
}
