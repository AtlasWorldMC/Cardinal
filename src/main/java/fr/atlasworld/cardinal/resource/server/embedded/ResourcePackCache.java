package fr.atlasworld.cardinal.resource.server.embedded;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import fr.atlasworld.cardinal.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class ResourcePackCache {
    private static final Logger LOGGER = Logging.logger();
    private static final Gson GSON = new Gson();

    public static final File CACHE_INDEX_FILE = new File(".cache/resourcepacks/index.json");

    private JsonObject cache;

    public void load() throws IOException {
        if (!CACHE_INDEX_FILE.exists()) {
            this.cache = new JsonObject();
            this.save();
            return;
        }

        try (InputStream stream = new FileInputStream(CACHE_INDEX_FILE);
             InputStreamReader reader = new InputStreamReader(stream)) {
            this.cache = JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public void save() throws IOException {
        if (!CACHE_INDEX_FILE.exists()) {
            CACHE_INDEX_FILE.getParentFile().mkdirs();
            CACHE_INDEX_FILE.createNewFile();
        }

        try (OutputStream stream = new FileOutputStream(CACHE_INDEX_FILE); OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.write(GSON.toJson(this.cache));
            writer.flush();
        }
    }

    private void saveAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                this.save();
            } catch (IOException e) {
                LOGGER.error("Failed to save resource pack cache:", e);
            }
        });
    }

    @SuppressWarnings("deprecation")
    public boolean isCached(PluginClassLoader loader, File resourcePackFile) {
        if (!this.cache.has(loader.meta().identifier()))
            return false;

        if (!resourcePackFile.exists())
            return false;

        try {
            ResourcePackCacheEntry entry = ResourcePackCacheEntry.fromJson(loader.meta().identifier(), this.cache.getAsJsonObject(loader.meta().identifier()));

            HashCode packHash = Files.asByteSource(resourcePackFile).hash(Hashing.sha1());
            HashCode pluginHash = loader.hash();

            return entry.matches(packHash.toString(), pluginHash.toString());
        } catch (Throwable ex) {
            LOGGER.error("Failed to check if resource pack '{}' is cached:", loader.meta().identifier(), ex);
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    public void cache(PluginClassLoader loader, File resourcePackFile) {
        try {
            HashCode packHash = Files.asByteSource(resourcePackFile).hash(Hashing.sha1());
            HashCode pluginHash = loader.hash();

            ResourcePackCacheEntry entry = new ResourcePackCacheEntry(loader.meta().identifier(), pluginHash.toString(), packHash.toString());
            entry.append(this.cache);

            this.saveAsync();
        } catch (Throwable ex) {
            LOGGER.error("Failed to cache local resource pack '{}':", loader.meta().identifier(), ex);
        }
    }

    @SuppressWarnings("deprecation")
    public HashCode retrievePackHash(PluginClassLoader loader, File resourcePackFile) {
        try {
            if (this.cache.has(loader.meta().identifier()))
                return HashCode.fromString(this.cache.getAsJsonObject(loader.meta().identifier()).get("pack").getAsString());

            return Files.asByteSource(resourcePackFile).hash(Hashing.sha1());
        } catch (Throwable ex) {
            LOGGER.error("Failed to retrieve hash for resource pack '{}':", loader.meta().identifier(), ex);
            return null;
        }
    }

    public record ResourcePackCacheEntry(@NotNull String identifier, @NotNull String pluginHash,
                                         @NotNull String packHash) {
        public void append(JsonObject root) {
            JsonObject entry = new JsonObject();

            entry.addProperty("plugin", this.pluginHash);
            entry.addProperty("pack", this.packHash);

            root.add(this.identifier, entry);
        }

        public boolean matches(@NotNull String packHash, @NotNull String pluginHash) {
            return this.packHash.equals(packHash) && this.pluginHash.equals(pluginHash);
        }

        public static ResourcePackCacheEntry fromJson(@NotNull String key, @NotNull JsonObject entry) {
            return new ResourcePackCacheEntry(key, entry.get("plugin").getAsString(), entry.get("pack").getAsString());
        }
    }
}
