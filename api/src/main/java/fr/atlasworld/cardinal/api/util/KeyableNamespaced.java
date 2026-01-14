package fr.atlasworld.cardinal.api.util;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Namespaced;
import org.jetbrains.annotations.NotNull;

/**
 * Utility interface extending the {@link Namespaced} to easily create keys.
 */
public interface KeyableNamespaced extends Namespaced {

    /**
     * Create a key with this namespace.
     *
     * @param name value / name of the key.
     *
     * @return newly created key.
     */
    default Key createKey(@NotNull String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkArgument(RegistryKey.isValidKey(name));

        return Key.key(this, name);
    }
}
