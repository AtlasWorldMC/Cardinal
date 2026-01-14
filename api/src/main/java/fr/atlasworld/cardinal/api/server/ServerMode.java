package fr.atlasworld.cardinal.api.server;

/**
 * Represents the mode the server can be started in.
 */
public enum ServerMode {

    /**
     * Development mode, the server is in a development environment and debug tools are enabled.
     */
    DEVELOPMENT,

    /**
     * Production mode, the server is started in a production environment, securities should be enabled and debug tools disabled.
     */
    PRODUCTION,

    /**
     * Data Generation mode, server is started exclusively for data generation and will never fully initialize.
     * <br><br>
     * <b>Warning:</b> the API is extremely limited and most of the main parts, such as managers, are not initialized!
     */
    DATA_GENERATION;
}
