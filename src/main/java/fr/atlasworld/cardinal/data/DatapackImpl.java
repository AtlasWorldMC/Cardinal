package fr.atlasworld.cardinal.data;

import com.google.common.base.Preconditions;
import com.google.gson.*;
import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.util.Serializers;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DatapackImpl implements Datapack {
    public static final String PACK_META = "pack.json";
    public static final String VANILLA_META = "pack.mcmeta";

    private final @NotNull Component name;
    private final @NotNull String version;
    private final @NotNull Component description;
    private final @NotNull Set<Component> authors;

    private final ZipFile file;
    private final DataSource source;

    public DatapackImpl(@NotNull ZipFile file, @NotNull DataSource source) throws JsonParseException, IOException {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(source);

        this.file = file;
        this.source = source;

        ZipEntry metaEntry = file.getEntry(PACK_META);
        try (InputStream stream = file.getInputStream(metaEntry); InputStreamReader reader = new InputStreamReader(stream)) {
            try {
                JsonObject metaObject = JsonParser.parseReader(reader).getAsJsonObject();

                this.name = Serializers.MINI_MESSAGE.deserialize(metaObject.get("name").getAsString());
                this.version = metaObject.get("version").getAsString();
                this.description = metaObject.has("description") ? Serializers.MINI_MESSAGE.deserialize(metaObject.get("description").getAsString()) : Component.empty();
                this.authors = metaObject.get("authors").getAsJsonArray().asList().stream().map(JsonElement::getAsString)
                        .map(Serializers.MINI_MESSAGE::deserialize).collect(Collectors.toUnmodifiableSet());
            } catch (Throwable ex) {
                throw new JsonSyntaxException("Failed to parse datapack metadata", ex);
            }
        } catch (NullPointerException ex) {
            if (file.getEntry(VANILLA_META) != null)
                throw new UnsupportedOperationException("Vanilla datapacks are not supported.");

            throw new FileNotFoundException("Datapack metadata file not found.");
        }
    }

    public @NotNull Enumeration<? extends ZipEntry> entries() {
        return this.file.entries();
    }

    public @Nullable InputStream openStream(@NotNull ZipEntry entry) throws IOException {
        return this.file.getInputStream(entry);
    }

    public @Nullable ZipEntry getEntry(@NotNull String name) {
        return this.file.getEntry(name);
    }

    public @NotNull String filename() {
        return this.file.getName();
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

    @Override
    public @NotNull DataSource source() {
        return this.source;
    }

    @Override
    public String toString() {
        return "DatapackImpl{" +
                "name=" + Serializers.PLAIN_TEXT.serialize(this.name) +
                ", version='" + this.version + '\'' +
                ", file=" + this.file.getName() +
                '}';
    }
}
