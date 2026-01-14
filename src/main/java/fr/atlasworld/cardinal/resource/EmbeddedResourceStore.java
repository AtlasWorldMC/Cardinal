package fr.atlasworld.cardinal.resource;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.DataEntry;
import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.data.CardinalDataManager;
import fr.atlasworld.cardinal.data.source.EmbeddedDataEntry;
import fr.atlasworld.cardinal.resource.entity.EmbeddedResourceEntry;
import fr.atlasworld.cardinal.resource.entity.ResourceSource;
import fr.atlasworld.cardinal.util.Logging;
import fr.atlasworld.fresco.source.ResourceEntry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class EmbeddedResourceStore implements DataSource, ResourceSource {
    private static final Logger LOGGER = Logging.logger();

    public static final String DATA_PATH_PREFIX = "data";
    public static final String RESOURCE_PATH_PREFIX = "assets";

    private final Map<String, DataEntry> dataEntries;
    private final Map<String, ResourceEntry> resourceEntries;

    public EmbeddedResourceStore() {
        this.dataEntries = new HashMap<>();
        this.resourceEntries = new HashMap<>();
    }

    public void collectEntries(@NotNull ZipFile file, @NotNull ClassLoader loader, @NotNull Plugin plugin) {
        Preconditions.checkNotNull(file, "File cannot be null!");
        Preconditions.checkNotNull(loader, "Loader cannot be null!");
        Preconditions.checkNotNull(plugin, "Plugin cannot be null!");

        Enumeration<? extends ZipEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            try {
                if (entry.isDirectory())
                    continue;

                if (entry.getName().startsWith(DATA_PATH_PREFIX)) {
                    this.collectDataEntry(entry, loader, plugin);
                    continue;
                }

                if (entry.getName().startsWith(RESOURCE_PATH_PREFIX)) {
                    this.collectResourceEntry(entry, loader);
                }
            } catch (Throwable ex) {
                LOGGER.error("Failed to collect entry '{}':", entry.getName(), ex);
            }
        }
    }

    private void collectDataEntry(ZipEntry entry, ClassLoader loader, Plugin plugin) throws IOException {
        if (entry.getName().endsWith(CardinalDataManager.META_ENTRY_EXTENSION))
            return;

        // TODO: Change DataPack format to data/<namespace>/<type>/<key>
        EmbeddedDataEntry dataEntry = new EmbeddedDataEntry(loader, plugin, entry.getName());
        this.dataEntries.put(entry.getName(), dataEntry);
    }

    private void collectResourceEntry(ZipEntry entry, ClassLoader loader) throws IOException {
        EmbeddedResourceEntry resourceEntry = new EmbeddedResourceEntry(loader, entry.getName());
        this.resourceEntries.put(entry.getName(), resourceEntry);
    }

    @Override
    public @NotNull Set<DataEntry> dataEntries() {
        return Set.copyOf(this.dataEntries.values());
    }

    @Override
    public boolean dataEntryPresent(@NotNull String path) {
        Preconditions.checkNotNull(path, "Path cannot be null");

        return this.dataEntries.containsKey(path);
    }

    @Override
    public Optional<DataEntry> dataEntry(@NotNull String path) {
        Preconditions.checkNotNull(path, "Path cannot be null");

        return Optional.ofNullable(this.dataEntries.get(path));
    }

    @Override
    public @NotNull Set<ResourceEntry> resourceEntries() {
        return Set.copyOf(this.resourceEntries.values());
    }

    @Override
    public boolean resourceEntryPresent(@NotNull String path) {
        Preconditions.checkNotNull(path, "Path cannot be null");

        return this.resourceEntries.containsKey(path);
    }

    @Override
    public @NotNull Optional<ResourceEntry> resourceEntry(@NotNull String path) {
        Preconditions.checkNotNull(path, "Path cannot be null");

        return Optional.ofNullable(this.resourceEntries.get(path));
    }
}
