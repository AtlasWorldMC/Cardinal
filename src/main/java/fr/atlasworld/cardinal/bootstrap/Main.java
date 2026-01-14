package fr.atlasworld.cardinal.bootstrap;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.util.Logging;

public class Main {
    public static final long START_TIME = System.currentTimeMillis();
    private static CardinalServer server;

    public static void main(String[] args) {
        LaunchArguments.parse(args);

        server = new CardinalServer();

        server.initialize();
        server.load(false);
        server.start();
    }

    public static synchronized void shutdown() {
        try {
            server.shutdown(false);
        } catch (Throwable ex) {
            Logging.logger().error("Failed to gracefully shutdown server:", ex);
        }
    }

    public static synchronized void crash(String reason) {
        Logging.logger().error("[Fatal]: {}", reason);

        if (server == null)
            return;

        try {
            server.shutdown(true);
        } catch (Throwable ex) {
            Logging.logger().error("Server crash handler failed to gracefully shutdown server:", ex);
        }
    }

    public static synchronized void crash(String reason, Throwable cause) {
        Logging.logger().error("[Fatal]: {}", reason, cause);

        if (server == null)
            return;

        try {
            server.shutdown(true);
        } catch (Throwable ex) {
            Logging.logger().error("Server crash handler failed to gracefully shutdown server:", ex);
        }
    }
}