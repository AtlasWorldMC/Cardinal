package fr.atlasworld.cardinal.event;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.event.registry.RegistrationEvent;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.event.trait.InternalEvent;
import fr.atlasworld.cardinal.game.GameImpl;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.trait.EntityEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public final class EventNodeFactory {

    /**
     * Specific event node for normal behavior code, this should not have any children and should be executed last to
     * prevent executing code for canceled events.
     *
     * @return newly create server node.
     */
    public static EventNode<@NotNull Event> createServerNode() {
        EventNode<Event> node = EventNode.all("cardinal-server-listener").setPriority(Integer.MAX_VALUE); // Make sure it's executed last.
        MinecraftServer.getGlobalEventHandler().addChild(node);

        return node;
    }

    /**
     * Specific event node for registering events, this should not have any children and should be executed first to
     * to prevent plugin from hijacking the registered entries.
     *
     * @return newly create register node.
     */
    public static EventNode<@NotNull RegistrationEvent> createRegisterNode() {
        EventNode<RegistrationEvent> node = EventNode.type("cardinal-register-listener",
                EventFilter.from(RegistrationEvent.class, Registry.class, RegistrationEvent::registry));

        MinecraftServer.getGlobalEventHandler().addChild(node);
        return node;
    }

    /**
     * Plugin event node, any event nodes that the plugin uses should become children of this node.
     *
     * @param meta meta-data of the plugin.
     * @return newly created plugin node.
     */
    public static EventNode<@NotNull Event> createPluginNode(@NotNull PluginClassLoader.PluginMeta meta) {
        EventNode<@NotNull Event> node = EventNode.event("cardinal-plugin-" + meta.identifier(), EventFilter.ALL, event -> {
            if (event instanceof InternalEvent)
                return false;

            return !InternalEvent.BLACKLISTED_EVENTS.contains(event.getClass());
        });

        MinecraftServer.getGlobalEventHandler().addChild(node);
        return node;
    }

    public static EventNode<@NotNull Event> createGameNode(@NotNull GameImpl game, @NotNull GameContainer container) {
        if (game.plugin() != null)
            return createPluginGameNode(game.plugin(), container);

        return createBuiltinGameNode(container);
    }

    public static EventNode<@NotNull Event> createPluginGameNode(@NotNull Plugin plugin, @NotNull GameContainer container) {
        Preconditions.checkNotNull(plugin, "Plugin cannot be null!");
        Preconditions.checkNotNull(container, "Game container cannot be null!");

        PluginClassLoader loader = (PluginClassLoader) plugin.getClass().getClassLoader();
        EventNode<@NotNull Event> node = EventNode.event("game-container-" + container.identifier().toString().substring(0, 5), EventFilter.ALL, event -> {
            if (event instanceof InstanceEvent instanceEvent)
                return checkContainerInstance(container, instanceEvent.getInstance());

            if (event instanceof EntityEvent entityEvent)
                return checkContainerInstance(container, entityEvent.getEntity().getInstance());

            return true;
        });

        loader.context().eventNode().addChild(node);
        return node;
    }

    /**
     * Unsecure game event node, this should only be used for builtin games where no plugins are associated with.
     *
     * @param container container of the game.
     * @return newly created game node.
     */
    public static EventNode<@NotNull Event> createBuiltinGameNode(@NotNull GameContainer container) {
        Preconditions.checkNotNull(container, "Game container cannot be null!");

        EventNode<@NotNull Event> node = EventNode.event("game-container-" + container.identifier().toString().substring(0, 5), EventFilter.ALL, event -> {
            if (event instanceof InstanceEvent instanceEvent)
                return checkContainerInstance(container, instanceEvent.getInstance());

            if (event instanceof EntityEvent entityEvent)
                return checkContainerInstance(container, entityEvent.getEntity().getInstance());

            return true;
        });

        MinecraftServer.getGlobalEventHandler().addChild(node);
        return node;
    }

    private static boolean checkContainerInstance(GameContainer container, Instance instance) {
        if (container == null || instance == null)
            return false;

        return container.isInstanceLinked(instance);
    }
}
