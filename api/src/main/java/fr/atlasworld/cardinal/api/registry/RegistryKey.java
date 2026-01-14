package fr.atlasworld.cardinal.api.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Namespaced;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Registry Key, those are unique key composed of a {@code namespace} and a {@code key}.
 * <p>
 * A full registry key cannot exceed 32767 characters,
 * this is imposed by the minecraft protocol when sending those key to clients.
 */
// TODO: Rework the registry key system.
@ApiStatus.Obsolete // Prefer simply using the {@link Key} interface.
public final class RegistryKey implements Key {
    private final @NotNull String namespace;
    private final @NotNull String value;

    public RegistryKey(@NotNull Plugin plugin, @NotNull String value) {
        this(plugin.namespace(), value);
    }

    public RegistryKey(@NotNull String namespace, @NotNull String value) {
        Preconditions.checkNotNull(namespace, "Namespace may not be null!");
        Preconditions.checkNotNull(value, "Key must not be null!");

        Preconditions.checkArgument(isValidNamespace(namespace), "Invalid namespace, must be [a-z0-9._-]: %s", namespace);
        Preconditions.checkArgument(isValidKey(value), "Invalid value, must be [a-z0-9/._-]: %s", namespace);

        this.namespace = namespace;
        this.value = value;

        String fullKey = this.toString();
        Preconditions.checkArgument(fullKey.length() <= Short.MAX_VALUE, "Namespace must be less than 32768 characters.");
    }

    public RegistryKey(@NotNull Namespaced namespaced, @NotNull String value) {
        this(namespaced.namespace(), value);
    }

    @Override
    @KeyPattern.Namespace
    public @NotNull String namespace() {
        return this.namespace;
    }

    @Override
    @KeyPattern.Value
    public @NotNull String value() {
        return this.value;
    }

    @Override
    public @NotNull String asString() {
        return this.namespace + DEFAULT_SEPARATOR + this.value;
    }

    @Override
    public String toString() {
        return this.asString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Key key))
            return false;

        return this.namespace.equals(key.namespace()) && this.value.equals(key.value());
    }

    @Override
    public int hashCode() {
        int result = this.namespace.hashCode();
        result = (31 * result) + this.value.hashCode();
        return result;
    }

    /**
     * Parse a RegistryKey from a string.
     *
     * @param str string to parse the registry key from.
     * @return optional containing the registry key is the str could be parsed, empty optional if otherwise.
     * @see #fromString(String, String)
     */
    public static Optional<RegistryKey> fromString(@Nullable String str) {
        return fromString(str, null);
    }

    /**
     * Parse a RegistryKey from a string.
     *
     * @param str              string to parse the registry key from.
     * @param defaultNamespace default namespace in-case that the str does not contain a namespace,
     *                         in that case this namespace will be applied.
     * @return optional containing the registry key is the str could be parsed, empty optional if otherwise.
     */
    @NotNull
    public static Optional<RegistryKey> fromString(@Nullable String str, @Nullable String defaultNamespace) {
        if (str == null)
            return Optional.empty();

        if (str.isEmpty() || str.length() > Short.MAX_VALUE)
            return Optional.empty();

        String[] parts = str.split(":", 3);
        if (parts.length > 2)
            return Optional.empty();

        String namespace = parts.length == 2 ? parts[0] : defaultNamespace;
        if (!isValidNamespace(namespace))
            return Optional.empty();

        String key = parts.length == 2 ? parts[1] : parts[0];
        if (!isValidKey(key))
            return Optional.empty();

        return Optional.of(new RegistryKey(namespace, key));
    }

    /**
     * Wrap an existing {@link Key} into a {@link RegistryKey}.
     *
     * @param key key to wrap.
     * @return same instance cast into a {@link RegistryKey},
     * or a new instance {@link RegistryKey} with the same namespace and key.
     *
     * @deprecated Prefer simply using the {@link Key} instead of wrapping it, this allows for the same kind of features
     *             as this key implementation.
     */
    @Deprecated(forRemoval = true)
    public static RegistryKey wrap(@NotNull Key key) {
        if (key instanceof RegistryKey registryKey)
            return registryKey;

        return new RegistryKey(key.namespace(), key.value());
    }

    /**
     * Checks if the character is valid for a namespace.
     *
     * @param c character to check.
     * @return true if the namespace char is valid, false otherwise.
     */
    private static boolean isValidNamespaceChar(char c) {
        return switch (c) {
            case '.', '_', '-' -> true;
            default -> (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');
        };
    }

    /**
     * Checks if the character is valid for a key.
     *
     * @param c character to check.
     * @return true if the key char is valid, false otherwise.
     */
    private static boolean isValidKeyChar(char c) {
        return c == '/' || isValidNamespaceChar(c);
    }

    /**
     * Checks whether a namespace is valid.
     *
     * @param namespace namespace to check.
     * @return true if the namespace is valid, false otherwise.
     */
    public static boolean isValidNamespace(String namespace) {
        if (namespace == null || namespace.isEmpty())
            return false;

        for (int i = 0; i < namespace.length(); i++) {
            if (!isValidNamespaceChar(namespace.charAt(i)))
                return false;
        }

        return true;
    }

    /**
     * Checks whether a key is valid.
     *
     * @param key key to check.
     * @return true if the key is valid, false otherwise.
     */
    public static boolean isValidKey(String key) {
        if (key == null || key.isEmpty())
            return false;

        for (int i = 0; i < key.length(); i++) {
            if (!isValidKeyChar(key.charAt(i)))
                return false;
        }

        return true;
    }
}
