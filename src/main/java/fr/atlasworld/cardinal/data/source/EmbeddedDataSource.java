package fr.atlasworld.cardinal.data.source;

import fr.atlasworld.cardinal.api.data.DataEntry;
import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.data.CardinalDataManager;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * DataSource implementation for embedded datapacks inside the core program or external plugins.
 */
public final class EmbeddedDataSource implements DataSource {
    private static final String DATA_DIR = "data/";
    private static final Logger LOGGER = Logging.logger();

    private final Map<String, DataEntry> entries;

    public EmbeddedDataSource(ClassLoader loader, Datapack pack, File sourceFile) throws IOException {
        this.entries = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(sourceFile)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                try {
                    if (!entry.getName().startsWith(DATA_DIR))
                        continue;

                    if (entry.isDirectory())
                        continue;

                    if (entry.getName().split("/").length < AbstractDataEntry.MINIMUM_PATH_DEPTH)
                        continue;

                    if (entry.getName().endsWith(CardinalDataManager.META_ENTRY_EXTENSION))
                        continue;

                    EmbeddedDataEntry dataEntry = new EmbeddedDataEntry(loader, pack, entry.getName());
                    this.entries.put(entry.getName(), dataEntry);
                } catch (Throwable ex) {
                    LOGGER.error("Failed to load entry '{}' from '{}':", entry.getName(), sourceFile, ex);
                }
            }
        }
    }

    @Override
    public @NotNull Set<DataEntry> dataEntries() {
        return Set.copyOf(this.entries.values());
    }

    @Override
    public boolean dataEntryPresent(@NotNull String path) {
        return this.entries.containsKey(path);
    }

    @Override
    public Optional<DataEntry> dataEntry(@NotNull String path) {
        return Optional.ofNullable(this.entries.get(path));
    }
}
