package fr.atlasworld.cardinal.api.data;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a data entry.
 */
public interface DataEntry {

    /**
     * The filename of the data entry, including the extension.
     *
     * @return full filename of the data entry.
     */
    @NotNull String filename();

    /**
     * The key of the entry, usually it's the entry name without the namespace, type and file extension.
     * <p>
     * Ex: {@code /example/type/my_cool/entry.json} would become {@code my_cool/entry}
     *
     * @return the key of the entry.
     */
    @NotNull String key();

    /**
     * Type of the entry.
     *
     * @return type of the entry.
     */
    @NotNull String type();

    /**
     * The namespace of the entry.
     *
     * @return namespace of the entry.
     */
    @NotNull String namespace();

    /**
     * Retrieve the raw unparsed entry path.
     *
     * @return raw unparsed entry.
     */
    @NotNull String fullPath();

    /**
     * Checks whether the entry is inside the namespace of the datapack.
     * <br>
     * Meaning that there isn't a type associated with the entry, these are used for indirect {@link DataTypeOld}.
     *
     * @return {@code true} if the entry is inside the namespace, {@code false} otherwise.
     */
    default boolean isInsideNamespace() {
        return this.type().isEmpty();
    }

    /**
     * Retrieve the key of the entry.
     *
     * @return key of this entry.
     */
    default @NotNull Key retrieveKey() {
        return Key.key(this.namespace(), this.key());
    }

    /**
     * Get the entry as a source.
     *
     * @return data source.
     */
    @NotNull ResourceSource asSource();
}
