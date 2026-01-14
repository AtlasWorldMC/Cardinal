package fr.atlasworld.cardinal.util;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.bootstrap.LaunchArguments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * Utility class for logging purposes.
 */
public final class Logging {
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    public static final String ASCII = """
               ___                   _  _                _\s
              / __\\  __ _  _ __   __| |(_) _ __    __ _ | |
             / /    / _` || '__| / _` || || '_ \\  / _` || |
            / /___ | (_| || |   | (_| || || | | || (_| || |
            \\____/  \\__,_||_|    \\__,_||_||_| |_| \\__,_||_|
            (%s for Minecraft %s)
            """;

    static {
        if (LaunchArguments.devMode() || LaunchArguments.dataGen() != null) {
            Configurator.setAllLevels(LogManager.ROOT_LOGGER_NAME, org.apache.logging.log4j.Level.TRACE);
            logger().warn("Dev Feature: Lowered logging level to TRACE.");
        } else {
            disableLogger(net.minestom.server.instance.block.BlockManager.class.getName());
        }
    }

    /**
     * Retrieve a logger for the calling class.
     *
     * @return newly created logger with the name of the calling class.
     */
    @NotNull
    public static Logger logger() {
        return LoggerFactory.getLogger(WALKER.getCallerClass());
    }

    /**
     * Outputs multiple line with a logger.
     *
     * @param logger  logger to output the text.
     * @param message message / text block to write to the logger.
     */
    public static void logMultiline(@NotNull Logger logger, @NotNull String message, @NotNull Level level) {
        Preconditions.checkNotNull(logger);
        Preconditions.checkNotNull(message);

        for (String line : message.split("\n")) {
            logger.atLevel(level).log(line);
        }
    }

    public static void disableLogger(String loggerName) {
        Configurator.setAllLevels(loggerName, org.apache.logging.log4j.Level.OFF);
    }
}
