package fr.atlasworld.cardinal.bootstrap;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class BuildInfo {
    private static final String FILENAME = "build.properties";
    private static final Logger LOGGER = Logging.logger();

    private static final Properties properties;

    static {
        properties = new Properties();

        properties.put("version", "Undefined");
        properties.put("build_time", new Date(0));

        properties.put("name", "Undefined");
        properties.put("description", "Undefined");
        properties.put("authors", "Undefined");

        load(); // Load the info from the file.
    }

    private static void load() {
        ClassLoader loader = CardinalServer.class.getClassLoader();

        if (loader.getResource(FILENAME) == null) {
            LOGGER.error("Failed to load " + FILENAME + " file, proper version information will be unavailable.");
            return;
        }

        try (InputStream stream = loader.getResourceAsStream(FILENAME)) {
            properties.load(stream);
        } catch (Exception ex) {
            LOGGER.error("Failed to load " + FILENAME + " file, proper version information will be unavailable.", ex);
        }
    }

    @NotNull
    public static String version() {
        return (String) properties.get("version");
    }

    @NotNull
    public static Date buildTime() {
        try {
            return (Date) properties.get("build_time");
        } catch (ClassCastException e) {
            return new Date(Long.parseLong((String) properties.get("build_time")));
        }
    }

    public static String name() {
        return (String) properties.get("name");
    }

    public static String description() {
        return (String) properties.get("description");
    }

    public static Set<String> authors() {
        return Set.of((String) properties.get("authors"));
    }

    public static String printableTimeSinceBuild() {
        long difference = System.currentTimeMillis() - buildTime().getTime();

        long days = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS);
        long hours = TimeUnit.HOURS.convert(difference, TimeUnit.MILLISECONDS) % 24;
        long minutes = TimeUnit.MINUTES.convert(difference, TimeUnit.MILLISECONDS) % 60;

        if (days > 0)
            return days + " days";
        else if (hours > 0)
            return hours + " hours";
        else if (minutes > 0)
            return minutes + " minutes";
        else return "less than a minute";
    }

    @NotNull
    public static String print() {
        return String.format("Version: %s for Minecraft %s", version(), MinecraftServer.VERSION_NAME);
    }
}
