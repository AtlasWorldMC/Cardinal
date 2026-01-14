package fr.atlasworld.cardinal.server.world;

import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.api.server.world.StructurePool;
import fr.atlasworld.cardinal.util.Logging;
import net.hollowcube.schem.Schematic;
import net.hollowcube.schem.SchematicReadException;
import net.hollowcube.schem.SchematicReader;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class StructurePoolImpl implements StructurePool {
    private static final Logger LOGGER = Logging.logger();
    private static final Schematic EMPTY = new Schematic(Vec.ZERO, Vec.ZERO, new Block[0], new byte[0]);

    public static final SchematicReader READER = new SchematicReader();

    private final Map<Key, StructurePoolEntry> entries;
    private final Map<Key, Schematic> loadedSchematics;

    public StructurePoolImpl(Set<StructurePoolEntry> entries) {
        this.entries = entries.stream().collect(Collectors.toMap(StructurePoolEntry::key, entry -> entry));
        this.loadedSchematics = new ConcurrentHashMap<>(); // Generators **WILL** call this class concurrently!
    }

    @Override
    public @NotNull Schematic resolve() {
        return this.resolve(ThreadLocalRandom.current());
    }

    @Override
    public @NotNull Schematic resolve(@NotNull Random random) {
        return this.rollStructure(random);
    }

    public Schematic rollStructure(Random random) {
        int totalWeight = 0;
        for (StructurePoolEntry entry : this.entries.values()) {
            if (entry.isBroken()) continue;
            totalWeight += entry.weight;
        }

        if (totalWeight <= 0) {
            LOGGER.warn("No available structure found in pool, falling back to empty schematic.");
            return EMPTY;
        }

        int roll = random.nextInt(totalWeight);
        for (StructurePoolEntry entry : this.entries.values()) {
            if (entry.isBroken()) continue;
            roll -= entry.weight;

            if (roll > 0)
                continue;

            Schematic schematic = this.resolve(entry.key);
            if (schematic == null)
                return this.rollStructure(random);

            return schematic;
        }

        LOGGER.warn("No available structure found in pool, falling back to empty schematic.");
        return EMPTY;
    }

    public @Nullable Schematic resolve(@NotNull Key key) {
        if (this.loadedSchematics.containsKey(key))
            return this.loadedSchematics.get(key);

        try {
            return this.loadStructure(key);
        } catch (IOException ex) {
            LOGGER.error("Failed to open schematic file: {}", key, ex);
        } catch (SchematicReadException ex) {
            LOGGER.error("Corrupted or invalid schematic file: {}", key, ex);
        }

        this.entries.get(key).markBroken(); // Structure won't load.
        return null;
    }

    /**
     * Synchronized method to load structure if they're not present,
     * prevents edge cases where multiple threads would try to load the same schematic at the same time.
     *
     * @param key of the structure pool to load.
     *
     * @return the loaded schematic.
     *
     * @throws IOException if the schematic file could not be read.
     * @throws SchematicReadException if the schematic file is invalid.
     * @throws IllegalArgumentException if no structure was found for the given key.
     */
    private synchronized Schematic loadStructure(@NotNull Key key) throws IOException, SchematicReadException {
        if (this.loadedSchematics.containsKey(key))
            return this.loadedSchematics.get(key); // In case during the thread waiting, the schematic was loaded.

        StructurePoolEntry entry = this.entries.get(key);
        if (entry == null)
            throw new IllegalArgumentException("No structure found for key '" + key + "'");

        Schematic schematic = entry.load();
        this.loadedSchematics.put(key, schematic);

        return schematic;
    }

    @Override
    public Optional<Schematic> retrieveStructure(@NotNull Key key) {
        if (!this.entries.containsKey(key))
            return Optional.empty();

        if (this.entries.get(key).isBroken()) {
            LOGGER.warn("Structure '{}' was requested, but marked in a broken state.", key);
            return Optional.empty();
        }

        return Optional.ofNullable(this.resolve(key));
    }

    @Override
    public @NotNull Set<Key> structures() {
        return this.entries.keySet();
    }

    public static final class StructurePoolEntry {
        private final Key key;
        private final int weight;
        private final ResourceSource source;

        private volatile boolean broken;

        public StructurePoolEntry(Key key, int weight, ResourceSource source) {
            this.key = key;
            this.weight = weight;
            this.source = source;
        }

        public Key key() {
            return this.key;
        }

        public Schematic load() throws IOException, SchematicReadException {
            return READER.read(this.source.openStream());
        }

        // Used to determine whether the schematic is considered broken or not.
        public boolean isBroken() {
            return this.broken;
        }

        public void markBroken() {
            this.broken = true;
        }
    }
}
