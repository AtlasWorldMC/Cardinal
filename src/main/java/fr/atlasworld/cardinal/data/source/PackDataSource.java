package fr.atlasworld.cardinal.data.source;

import fr.atlasworld.cardinal.api.data.DataEntry;
import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.data.CardinalDataManager;
import fr.atlasworld.cardinal.data.DatapackImpl;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackDataSource implements DataSource {
    private static final Logger LOGGER = Logging.logger();

    private final DatapackImpl datapack;
    private final Map<String, DataEntry> entries;

    public PackDataSource(ZipFile file) throws IOException {
        this.datapack = new DatapackImpl(file, this);
        this.entries = new HashMap<>();

        Enumeration<? extends ZipEntry> entries = this.datapack.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            try {
                if (entry.isDirectory())
                    continue;

                if (entry.getName().split("/").length < AbstractDataEntry.MINIMUM_PATH_DEPTH)
                    continue;

                if (entry.getName().endsWith(CardinalDataManager.META_ENTRY_EXTENSION))
                    continue;

                PackDataEntry dataEntry = new PackDataEntry(this.datapack, entry);
                this.entries.put(entry.getName(), dataEntry);
            } catch (Throwable ex) {
                LOGGER.error("Failed to load entry '{}' from '{}':", entry.getName(), datapack.filename(), ex);
            }
        }
    }

    public @NotNull Datapack datapack() {
        return this.datapack;
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
