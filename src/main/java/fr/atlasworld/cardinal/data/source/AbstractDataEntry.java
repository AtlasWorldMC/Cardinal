package fr.atlasworld.cardinal.data.source;

import fr.atlasworld.cardinal.api.data.DataEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Abstract implementation of {@link DataEntry} that handles path parsing.
 */
public abstract class AbstractDataEntry implements DataEntry {
    public static final String DATA_DIR = "data/";
    public static final int MINIMUM_PATH_DEPTH = 2;

    private final String fullPath;
    private final String namespace;
    private final String type;
    private final String key;
    private final String filename;

    /**
     * @param path the full path from {@code ZipEntry#getName()}, usually starting with "data/".
     * @throws IllegalArgumentException if the path structure is invalid for a data entry.
     */
    protected AbstractDataEntry(@NotNull String path) {
        this.fullPath = Objects.requireNonNull(path, "Path cannot be null");

        String[] parts = parsePath(path);
        this.namespace = parts[0];
        this.type = parts[1];
        this.key = parts[2];
        this.filename = parts[3];
    }

    /**
     * Parses the raw path into its components.
     *
     * @param rawPath the raw path string.
     * @return an array containing {namespace, type, key, filename}.
     */
    // TODO: Test this code
    private static String[] parsePath(String rawPath) {
        String path = rawPath.replace('\\', '/');
        if (path.startsWith("/"))
            path = path.substring(1);

        if (!path.startsWith(DATA_DIR))
            throw new IllegalArgumentException("Path must start with 'data/': " + rawPath);

        String remaining = path.substring(DATA_DIR.length());

        // Namespace
        int firstSlash = remaining.indexOf('/');
        if (firstSlash == -1)
            throw new IllegalArgumentException("Path is not deep enough (missing namespace): " + rawPath);

        String namespace = remaining.substring(0, firstSlash);
        remaining = remaining.substring(firstSlash + 1);

        int secondSlash = remaining.indexOf('/');

        String type;
        String keyWithExtension;

        if (secondSlash == -1) {
            // Case: data/<namespace>/file.json
            // There is no "type" folder.
            type = "";
            keyWithExtension = remaining;
        } else {
            // Case: data/<namespace>/<type>/...
            type = remaining.substring(0, secondSlash);
            keyWithExtension = remaining.substring(secondSlash + 1);
        }

        if (keyWithExtension.isEmpty())
            throw new IllegalArgumentException("Path is not deep enough (missing key/filename): " + rawPath);

        // Filename
        int lastSlash = keyWithExtension.lastIndexOf('/');
        String filename = (lastSlash == -1) ? keyWithExtension : keyWithExtension.substring(lastSlash + 1);

        // Key
        int lastDot = keyWithExtension.lastIndexOf('.');
        String key;

        if (lastDot != -1 && lastDot > lastSlash) {
            key = keyWithExtension.substring(0, lastDot);
        } else {
            key = keyWithExtension;
        }

        return new String[]{namespace, type, key, filename};
    }

    @Override
    public @NotNull String filename() {
        return this.filename;
    }

    @Override
    public @NotNull String key() {
        return this.key;
    }

    @Override
    public @NotNull String type() {
        return this.type;
    }

    @Override
    public @NotNull String namespace() {
        return this.namespace;
    }

    @Override
    public @NotNull String fullPath() {
        return this.fullPath;
    }
}
