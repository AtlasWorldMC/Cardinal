package fr.atlasworld.cardinal.api.plugin;

import fr.atlasworld.cardinal.api.CardinalServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Represents a plugin context, this allows you to retrieve objects and set parameters you need.
 */
public interface PluginContext {

    /**
     * Retrieve the plugin specific logger.
     *
     * @return plugin specific logger.
     */
    @NotNull Logger logger();

    /**
     * Retrieve the plugin event node.
     *
     * @return plugin event node.
     */
    @NotNull EventNode<@NotNull Event> eventNode();

    /**
     * Retrieve the identifier of the plugin.
     *
     * @return plugin identifier.
     */
    @NotNull String identifier();

    /**
     * Retrieve the server.
     *
     * @return the server.
     */
    default @NotNull CardinalServer server() {
        return CardinalServer.getServer();
    }
}
