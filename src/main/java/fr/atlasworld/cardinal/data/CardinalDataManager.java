package fr.atlasworld.cardinal.data;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.api.data.*;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.registry.ReloadableRegistry;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.data.source.AbstractDataEntry;
import fr.atlasworld.cardinal.data.source.EmbeddedDataSource;
import fr.atlasworld.cardinal.data.source.PackDataSource;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipFile;

public class CardinalDataManager implements DataManager {
    public static final String META_ENTRY_EXTENSION = "meta";

    private static final File DATA_DIRECTORY = new File("datapacks");
    private static final Logger LOGGER = Logging.logger();

    static {
        if (!DATA_DIRECTORY.isDirectory())
            if (!DATA_DIRECTORY.mkdirs())
                Main.crash("Failed to create datapacks directory, please check file permissions.");
    }

    private final CardinalPluginManager pluginManager;
    private final CardinalServer server;
    private final Map<Datapack, DataSource> loadedPacks;

    private final Map<String, DataEntry> loadedDataEntries;

    public CardinalDataManager(CardinalServer server, CardinalPluginManager pluginManager) {
        this.server = server;
        this.pluginManager = pluginManager;

        this.loadedPacks = new HashMap<>();
        this.loadedDataEntries = new ConcurrentHashMap<>();
    }

    public void load(boolean reload) {
        if (reload) {
            this.loadedPacks.clear();
            this.loadedDataEntries.clear();
        }

        LOGGER.info("Collecting datapacks..");
        this.collectDatapacks();

        LOGGER.info("Loading data...");
        Map<DataType<?>, List<DataEntry>> entriesByType = this.collectEntries();

        entriesByType.entrySet().stream()
                .filter(entry -> this.shouldRegisterEntry(reload, entry.getKey()))
                .sorted(Comparator.<Map.Entry<DataType<?>, List<DataEntry>>>comparingInt(entry -> entry.getKey().priority()).reversed())
                .forEach(entry -> {
                    LOGGER.debug("Loading data of type '{}'...", entry.getKey().type());
                    entry.getValue().forEach(dataEntry -> this.handleDataEntry(dataEntry, entry.getKey()));
                });
    }

    private Map<DataType<?>, List<DataEntry>> collectEntries() {
        final HashMap<DataType<?>, List<DataEntry>> entriesByType = new HashMap<>();

        this.loadedPacks.values().stream()
                .sorted(Comparator.comparingInt(DataSource::priority).reversed())
                .forEach(source -> {
                    for (DataEntry entry : source.dataEntries()) {
                        Optional<DataType<?>> type = determineEntryType(entry);

                        if (type.isEmpty()) continue;
                        if (type.get().indexed()) {
                            // Indexed data types need access to all available entries of that type directly,
                            // se we load them all no matter if they're actually used or not.
                            //
                            // It's not a big issue but could be improved to only have them available temporarily
                            // while the indexed data type only loads the entries it accessed.
                            this.loadedDataEntries.put(entry.fullPath(), entry);
                        }

                        entriesByType.computeIfAbsent(type.get(), k -> new ArrayList<>()).add(entry);
                    }
                });

        return entriesByType;
    }

    private Optional<DataType<?>> determineEntryType(DataEntry entry) {
        if (entry.isInsideNamespace()) { // Possibly a direct data type index file.
            Optional<DataType<?>> dataType = CardinalRegistries.DATA_TYPES.retrieveIndexedType(entry.filename());
            if (dataType.isEmpty()) {
                LOGGER.warn("No data type for root index file: {}", entry.filename());
                return Optional.empty();
            }

            LOGGER.trace("Found index for data type '{}': {}", dataType.get().type(), entry.fullPath());
            return dataType;
        }

        String type = entry.type();
        Optional<DataType<?>> dataType = CardinalRegistries.DATA_TYPES.retrieveType(type);

        if (dataType.isEmpty()) {
            LOGGER.warn("No data type '{}' found for '{}', skipping...", type, entry.fullPath());
            return Optional.empty();
        }

        if (dataType.get().indexed()) { // Those
            this.loadedDataEntries.put(entry.fullPath(), entry);
            return Optional.empty();
        }

        LOGGER.trace("Found data '{}' entry: {}", dataType.get().type(), entry.fullPath());
        return dataType;
    }

    // Used for registries such as minestom's which elements can be registered to them only once.
    private boolean shouldRegisterEntry(boolean reload, DataType<?> dataType) {
        return !reload || dataType.registry() instanceof ReloadableRegistry<?>;
    }

    private @Nullable DataEntry openEntry(@NotNull Key key, @NotNull String path, @NotNull String extensions) {
        Preconditions.checkNotNull(key, "Key cannot be null!");
        Preconditions.checkNotNull(path, "Path cannot be null!");
        Preconditions.checkArgument(!path.isEmpty(), "Path cannot be empty!");

        String entryPath = AbstractDataEntry.DATA_DIR + key.namespace() + "/" + path + "/" + key.value() + "." + extensions;
        return this.loadedDataEntries.get(entryPath);
    }

    @Override
    public @NotNull Set<Datapack> loadedDatapacks() {
        return Set.copyOf(this.loadedPacks.keySet());
    }

    public @NotNull DataSource source(@NotNull Datapack datapack) {
        return this.loadedPacks.get(datapack);
    }

    private <T> void handleDataEntry(DataEntry entry, DataType<T> type) {
        final DataContext<T> ctx = new DataContext<>(type, this);

        try {
            type.load(entry, entry.retrieveKey(), ctx);
        } catch (Throwable ex) {
            LOGGER.error("Failed to load data entry '{}':", entry.fullPath(), ex);
        }
    }

    private void collectDatapacks() {
        // Core pack
        try {
            ClassLoader coreLoader = CardinalServer.class.getClassLoader();
            File coreFile = new File(CardinalServer.class.getProtectionDomain().getCodeSource().getLocation().getFile());

            DataSource coreSource = new EmbeddedDataSource(coreLoader, this.server, coreFile);
            this.loadedPacks.put(this.server, coreSource);
        } catch (Throwable ex) {
            Main.crash("Fail to load core datapack:", ex);
        }

        // Plugins
        Set<Plugin> plugins = this.pluginManager.loadedPlugins();
        for (Plugin plugin : plugins) {
            PluginClassLoader loader = (PluginClassLoader) plugin.getClass().getClassLoader();
            this.loadedPacks.put(plugin, loader.store());
            LOGGER.info("Found datapack from plugin '{}'", loader.meta().identifier());
        }

        // Datapack
        Set<File> datapackCandidate = this.searchDatapackFiles();
        for (File candidate : datapackCandidate) {
            try {
                ZipFile zipFile = new ZipFile(candidate);

                PackDataSource source = new PackDataSource(zipFile);
                this.loadedPacks.put(source.datapack(), source);

                LOGGER.info("Found datapack '{}'", candidate.getName());
            } catch (IOException ex) {
                LOGGER.error("Failed to load datapack '{}'", candidate, ex);
            }
        }
    }

    private Set<File> searchDatapackFiles() {
        File[] datapackFiles = DATA_DIRECTORY.listFiles(file -> file.isFile() && file.getName().endsWith(".zip"));
        if (datapackFiles == null || datapackFiles.length == 0)
            return Set.of();

        return Set.of(datapackFiles);
    }

    private record DataContext<T>(DataType<T> type, CardinalDataManager manager) implements DataType.Context<T> {
        @Override
        public @NotNull Registry<T> registry() {
            return this.type.registry();
        }

        @Override
        public @NotNull Optional<DataEntry> openEntry(@NotNull Key key, @NotNull String extension) {
            return Optional.ofNullable(this.manager.openEntry(key, this.type.type(), extension));
        }
    }
}