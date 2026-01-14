package fr.atlasworld.cardinal.api;

import fr.atlasworld.cardinal.api.command.CommandManager;
import fr.atlasworld.cardinal.api.data.DataManager;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.game.GameManager;
import fr.atlasworld.cardinal.api.plugin.PluginManager;
import fr.atlasworld.cardinal.api.resource.ResourceManager;
import fr.atlasworld.cardinal.api.server.ServerMode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the underlying server, exposing some api's and managers to plugins.
 */
public abstract class CardinalServer implements Datapack {
    private static CardinalServer instance;

    /**
     * Retrieve the server instance.
     *
     * @return server instance.
     */
    public static @NotNull CardinalServer getServer() {
        return instance;
    }

    /**
     * Helper method to retrieve whether the server is running in production.
     * <br><br>
     * This should be used to disable some experimental or debugging features to help with development.
     *
     * @return {@code true} if the server is in production mode, {@code false} otherwise.
     */
    public static boolean isProduction() {
        return instance.serverMode() ==  ServerMode.PRODUCTION;
    }

    /**
     * Retrieve the plugin manager.
     *
     * @return plugin manager
     */
    public abstract @NotNull PluginManager pluginManager();

    /**
     * Retrieve the data manager.
     *
     * @return data manager.
     */
    public abstract @NotNull DataManager dataManager();

    /**
     * Retrieve the resource manager.
     *
     * @return resource manager.
     */
    public abstract @NotNull ResourceManager resourceManager();

    /**
     * Retrieve the game manager.
     *
     * @return game manager.
     */
    public abstract @NotNull GameManager gameManager();

    /**
     * Retrieve the command manager.
     *
     * @return command manager.
     */
    public abstract @NotNull CommandManager commandManager();

    /**
     * Retrieve the current mode the server is in.
     *
     * @return server mode.
     */
    public abstract ServerMode serverMode();
}
