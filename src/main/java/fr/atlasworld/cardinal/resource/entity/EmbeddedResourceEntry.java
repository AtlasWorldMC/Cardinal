package fr.atlasworld.cardinal.resource.entity;

import com.google.common.base.Preconditions;
import fr.atlasworld.fresco.source.ResourceEntry;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class EmbeddedResourceEntry implements ResourceEntry {
    public static final int ELEMENT_COUNT = 4; // 3 elements + 1 for the /data dir

    private static final int NAMESPACE_INDEX = 1;
    private static final int TYPE_INDEX = 2;
    private static final int KEY_INDEX = 3;

    private final String[] pathElements;
    private final String entry;
    private final ClassLoader loader;

    public EmbeddedResourceEntry(@NotNull ClassLoader loader, @NotNull String entry) {
        Preconditions.checkNotNull(loader, "Loader cannot be null!");
        Preconditions.checkNotNull(entry, "Entry cannot be null!");

        this.loader = loader;
        this.entry = entry;
        this.pathElements = entry.split("/", ELEMENT_COUNT);

        Preconditions.checkArgument(this.pathElements.length >= ELEMENT_COUNT - 1, "Invalid entry: %s", this.entry);
    }


    @Override
    public @NotNull String filename() {
        return this.entry.substring(this.entry.lastIndexOf('/') + 1);
    }

    @Override
    public @NotNull String key() {
        if (this.isInsideNamespace())
            return this.filename();

        String key = this.pathElements[KEY_INDEX];
        return key.substring(0, key.lastIndexOf('.'));
    }

    @Override
    public @NotNull String type() {
        return this.pathElements[TYPE_INDEX];
    }

    @Override
    public @NotNull String namespace() {
        return this.pathElements[NAMESPACE_INDEX];
    }

    @Override
    public @NotNull String fullPath() {
        return this.entry;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isInsideNamespace() {
        return this.pathElements.length == ELEMENT_COUNT - 1;
    }

    @Override
    public @NotNull InputStream openStream() throws IOException {
        InputStream stream = this.loader.getResourceAsStream(this.entry);
        if (stream == null)
            throw new IOException("Resource not found: " + this.entry);

        return stream;
    }
}
