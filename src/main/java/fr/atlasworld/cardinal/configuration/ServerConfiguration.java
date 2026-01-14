package fr.atlasworld.cardinal.configuration;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.Auth;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.function.Predicate;

public class ServerConfiguration {
    private static final Logger LOGGER = Logging.logger();
    private static final String DEFAULT_FILE = "server-default.toml";
    private static final String CONFIG_FILE = "server.toml";

    private static final ConfigSpec CONFIG_SPEC;

    static {
        CONFIG_SPEC = new ConfigSpec();

        // Network
        CONFIG_SPEC.define("network.address", "0.0.0.0");
        CONFIG_SPEC.defineInRange("network.port", 25565, 0, 65535);
        CONFIG_SPEC.define("network.authentication", Authentication.MOJANG.toString(), new AuthEnumValidator());
        CONFIG_SPEC.define("network.proxy-secret", "");
        CONFIG_SPEC.define("network.experimental-networking", false);

        // Game
        CONFIG_SPEC.defineInRange("game.render-distance", 12, 0, 32);
        CONFIG_SPEC.defineInRange("game.entity-distance", 7, 0, 32);
        CONFIG_SPEC.define("game.ticking-threads", -1);

        // Web Server
        CONFIG_SPEC.define("web-server.base-url", "http://localhost:8080/");
        CONFIG_SPEC.define("web-server.embedded.enabled", true);
        CONFIG_SPEC.define("web-server.embedded.address", "0.0.0.0");
        CONFIG_SPEC.defineInRange("web-server.embedded.port", 8080, 0, 65535);

        // Integration
        CONFIG_SPEC.define("integration.network", false);
    }

    private final FileConfig configuration;

    public ServerConfiguration() {
        this.configuration = FileConfig.builder(CONFIG_FILE)
                .charset(StandardCharsets.UTF_8)
                .onFileNotFound(FileNotFoundAction.copyData(CardinalServer.class.getClassLoader().getResource(DEFAULT_FILE)))
                .sync()
                .build();

        this.configuration.load();
        if (!CONFIG_SPEC.isCorrect(this.configuration)) {
            LOGGER.error("Server configuration is incorrect, correcting configuration...");
            CONFIG_SPEC.correct(this.configuration, (action, path, incorrectValue, correctedValue) -> {
                switch (action) {
                    case ADD -> LOGGER.warn("Added '{}' to configuration.", path);
                    case REMOVE -> LOGGER.warn("Removed '{}' from configuration.", path);
                    case REPLACE ->
                            LOGGER.warn("Replaced '{}' in '{}' with '{}' from configuration.", path, incorrectValue, correctedValue);
                }
            });

            this.configuration.save();
        }
    }

    public void apply() {
        // Flags
        System.setProperty("minestom.chunk-view-distance", String.valueOf(this.renderDistance()));
        System.setProperty("minestom.entity-view-distance", String.valueOf(this.entityDistance()));
        System.setProperty("minestom.dispatcher-threads", String.valueOf(this.tickingThreads()));

        if (this.experimentalNetworking()) {
            LOGGER.warn("Experimental networking is enabled.");
            System.setProperty("minestom.new-socket-write-lock", String.valueOf(true));
        }
    }

    // Network

    @NotNull
    public String serverAddress() {
        return this.configuration.get("network.address");
    }

    public int serverPort() {
        return this.configuration.getInt("network.port");
    }

    @NotNull
    public Authentication authentication() {
        Authentication authentication = this.configuration.getEnum("network.authentication", Authentication.class);
        return authentication == null ? Authentication.MOJANG : authentication;
    }

    @NotNull
    public String proxySecret() {
        return this.configuration.get("network.proxy-secret");
    }

    // Game play
    public int renderDistance() {
        return this.configuration.getInt("game.render-distance");
    }

    public int entityDistance() {
        return this.configuration.getInt("game.entity-distance");
    }

    public int tickingThreads() {
        int tickingThreads =  this.configuration.getInt("game.ticking-threads");

        if (tickingThreads < 1)
            tickingThreads = Runtime.getRuntime().availableProcessors();

        return tickingThreads;
    }

    public boolean experimentalNetworking() {
        return this.configuration.get("network.experimental-networking");
    }

    // Web Server
    public @NotNull String webServerBaseUrl() {
        return this.configuration.get("web-server.base-url");
    }

    public boolean enableEmbeddedWebServer() {
        return this.configuration.get("web-server.embedded.enabled");
    }

    public @NotNull String embeddedWebServerAddress() {
        return this.configuration.get("web-server.embedded.address");
    }

    public int embeddedWebServerPort() {
        return this.configuration.getInt("web-server.embedded.port");
    }

    // Network Integration

    public boolean networkIntegration() {
        return this.configuration.get("integration.network");
    }

    @NotNull
    public Config getConfiguration() {
        return this.configuration;
    }

    private static final class AuthEnumValidator implements Predicate<Object> {
        @Override
        public boolean test(Object obj) {
            try {
                Authentication.valueOf(String.valueOf(obj));
                return true;
            } catch (Throwable e) {
                return false;
            }
        }
    }

    public enum Authentication {
        OFFLINE,
        MOJANG,
        LEGACY,
        MODERN;

        public static Auth determineAuth(ServerConfiguration configuration) {
            return switch (configuration.authentication()) {
                case OFFLINE:
                    yield new Auth.Offline();
                case MOJANG:
                    yield new Auth.Online();
                case LEGACY: {
                    LOGGER.warn("Legacy player forwarding is deprecated and should not be used.");
                    yield new Auth.Bungee(Set.of(configuration.proxySecret()));
                }
                case MODERN:
                    yield new Auth.Velocity(configuration.proxySecret());
                default:
                    throw new IllegalStateException("Unknown authentication type: " + configuration.authentication());
            };
        }
    }
}
