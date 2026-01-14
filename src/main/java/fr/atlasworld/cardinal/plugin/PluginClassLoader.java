package fr.atlasworld.cardinal.plugin;

import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.Files;
import com.google.gson.*;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.CardinalPlugin;
import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.data.Meta;
import fr.atlasworld.cardinal.api.exception.plugin.PluginInitialLoadException;
import fr.atlasworld.cardinal.api.exception.plugin.PluginInitializationException;
import fr.atlasworld.cardinal.api.plugin.PluginContext;
import fr.atlasworld.cardinal.api.plugin.internal.PluginLoader;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.util.Serializers;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.data.CardinalDataManager;
import fr.atlasworld.cardinal.event.EventNodeFactory;
import fr.atlasworld.cardinal.resource.EmbeddedResourceStore;
import fr.atlasworld.cardinal.util.Hashable;
import fr.atlasworld.fresco.pack.PackMeta;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

public class PluginClassLoader extends URLClassLoader implements PluginLoader, Hashable {
    private static final String META_FILE = "plugin.json";

    private final Path source;
    private final PluginMeta meta;
    private final EmbeddedResourceStore store;
    private final GroupClassLoader groupLoader; // DO NOT REGISTER THIS LOADER TO THE GROUP LOADER INSIDE THIS CLASS.

    private @UnknownNullability CardinalPlugin plugin;

    public PluginClassLoader(@NotNull Path source, GroupClassLoader groupLoader) throws PluginInitialLoadException {
        super(determineUrl(source));

        this.source = source;
        this.groupLoader = groupLoader;
        this.store = new EmbeddedResourceStore();

        try (InputStream stream = this.getResourceAsStream(META_FILE)) {
            if (stream == null)
                throw new PluginInitialLoadException(source.getFileName().toString(), "Is not a Cardinal plugin!");

            this.meta = new PluginMeta(stream);
        } catch (IOException ex) {
            throw new PluginInitialLoadException(source.getFileName().toString(), "Could not retrieve '" + META_FILE + "' file!");
        }
    }

    public @NotNull Path source() {
        return this.source;
    }

    public @UnknownNullability CardinalPlugin plugin() {
        return this.plugin;
    }

    public @NotNull EmbeddedResourceStore store() {
        return this.store;
    }

    public void initiatePlugin() throws PluginInitializationException {
        Preconditions.checkState(this.plugin == null, "Plugin already initialized");

        try {
            Class<? extends CardinalPlugin> mainClass = this.loadClass(this.meta.mainClass(), false, false, false).asSubclass(CardinalPlugin.class);
            Constructor<? extends CardinalPlugin> constructor = mainClass.getDeclaredConstructor();

            constructor.setAccessible(true);
            try {
                this.plugin = constructor.newInstance();
            } catch (Throwable ex) {
                throw new PluginInitializationException("Failed to initiate plugin", ex, this.meta.identifier());
            }
        } catch (ClassCastException ex) {
            throw new PluginInitializationException("Plugin main class must extend CardinalPlugin", this.meta.identifier());
        } catch (ClassNotFoundException ex) {
            throw new PluginInitializationException("Plugin main class '" + this.meta.mainClass() + "' not found", ex, this.meta.identifier());
        } catch (NoSuchMethodException ex) {
            throw new PluginInitializationException("Plugin main class '" + this.meta.mainClass() + "' constructor should not take parameters as input", ex, this.meta.identifier());
        }
    }

    public void loadPacks() throws PluginInitialLoadException {
        try (ZipFile file = new ZipFile(source.toFile())) {
            this.store.collectEntries(file, this, this.plugin);
        } catch (IOException ex) {
            throw new PluginInitialLoadException(this.meta.identifier(), "Could not collect embedded data and resource packs.");
        }
    }

    private static URL[] determineUrl(Path source) {
        try {
            return new URL[]{source.toUri().toURL()};
        } catch (MalformedURLException | NullPointerException e) {
            Main.crash("Failed to create PluginClassLoader, file url was invalid:", e);
            return new URL[0]; // Will never be reached.
        }
    }

    public Class<?> loadClass(String name, boolean resolve, boolean checkRoot, boolean checkPlugins) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException ignored) {
        }

        if (checkRoot) {
            try {
                return this.groupLoader.loadClass(name, resolve, true, false);
            } catch (ClassNotFoundException ignored) {
            }
        }

        if (checkPlugins) {
            try {
                return this.groupLoader.loadClass(name, resolve, false, true);
            } catch (ClassNotFoundException ignored) {
            }
        }

        throw new ClassNotFoundException(name);
    }

    @Override
    public @NotNull PluginContext context() {
        return new Context(this.meta);
    }

    @Override
    public @NotNull PluginMeta meta() {
        return this.meta;
    }

    @Override
    public @UnknownNullability DataSource dataSource() {
        return ((CardinalDataManager) CardinalServer.instance().dataManager()).source(this.plugin);
    }

    @Override
    public @NotNull HashCode hash(@NotNull HashFunction function) throws IOException {
        return Files.asByteSource(this.source.toFile()).hash(function);
    }

    public static class PluginMeta implements Meta {
        private final @NotNull String identifier;
        private final @NotNull String mainClass;
        private final @NotNull Component name;
        private final @NotNull String version;
        private final @NotNull Component description;
        private final @NotNull Set<Component> authors;

        private PluginMeta(@NotNull InputStream stream) throws JsonParseException {
            try (InputStreamReader reader = new InputStreamReader(stream)) {
                JsonObject metaObject = JsonParser.parseReader(reader).getAsJsonObject();

                this.identifier = metaObject.get("identifier").getAsString();
                this.mainClass = metaObject.get("main_class").getAsString();
                this.name = Serializers.MINI_MESSAGE.deserialize(metaObject.get("name").getAsString());
                this.version = metaObject.get("version").getAsString();
                this.description = metaObject.has("description") ? Serializers.MINI_MESSAGE.deserialize(metaObject.get("description").getAsString()) : Component.empty();
                this.authors = metaObject.get("authors").getAsJsonArray().asList().stream().map(JsonElement::getAsString)
                        .map(Serializers.MINI_MESSAGE::deserialize).collect(Collectors.toUnmodifiableSet());

                Preconditions.checkArgument(RegistryKey.isValidNamespace(identifier), "Plugin identifier is invalid!");
            } catch (Throwable ex) {
                throw new JsonSyntaxException("Failed to parse plugin metadata", ex);
            }
        }


        public @NotNull String identifier() {
            return this.identifier;
        }

        public @NotNull String mainClass() {
            return this.mainClass;
        }

        @Override
        public @NotNull Component name() {
            return this.name;
        }

        @Override
        public @NotNull Component description() {
            return this.description;
        }

        @Override
        public @NotNull String version() {
            return this.version;
        }

        @Override
        public @NotNull Set<Component> authors() {
            return this.authors;
        }

        public @NotNull PackMeta asPackMeta() {
            return new PackMeta(Serializers.PLAIN_TEXT.serialize(this.description), MinecraftServer.RESOURCE_PACK_VERSION);
        }
    }

    public static class Context implements PluginContext {
        private final @NotNull Logger logger;
        private final @NotNull EventNode<Event> node;
        private final @NotNull String identifier;

        public Context(@NotNull PluginClassLoader.PluginMeta meta) {
            this.logger = LoggerFactory.getLogger(meta.identifier());
            this.node = EventNodeFactory.createPluginNode(meta);
            this.identifier = meta.identifier();
        }

        @Override
        public @NotNull Logger logger() {
            return this.logger;
        }

        @Override
        public @NotNull EventNode<Event> eventNode() {
            return this.node;
        }

        @Override
        public @NotNull String identifier() {
            return this.identifier;
        }
    }
}
