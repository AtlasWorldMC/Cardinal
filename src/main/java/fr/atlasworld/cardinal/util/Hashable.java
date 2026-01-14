package fr.atlasworld.cardinal.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Utility interface available for objects using files as their source, and allows to hash the content of the file.
 */
public interface Hashable {

    /**
     * Hash the content of the file using the provided hash function.
     *
     * @param function hash function to use.
     * @return Hashcode computed from the file content.
     * @throws IOException if the file could not be read.
     */
    @NotNull HashCode hash(@NotNull HashFunction function) throws IOException;

    // SHA-1 is deprecated, but it minecraft still uses it for resource packs.
    // And it's quite fast and unique enough that it won't create any collisions, DO NOT USE FOR SECURITY.
    @SuppressWarnings("deprecation")
    default @NotNull HashCode hash() throws IOException {
        return this.hash(Hashing.sha1());
    }
}
