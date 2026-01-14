package fr.atlasworld.cardinal.api.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The Registry holds all references to registered entries.
 * <p>
 * When initialized, the registry is not finalized,
 * and you are still able to register entries to it.
 * <br>
 * It's recommended that after your {@link fr.atlasworld.cardinal.api.event.trait.RegistryEvent} is called,
 * you finalize the registry using {@link #freezeRegistry()}.
 * <br>
 * In multithreaded environments,
 * finalizing the registry allows multiple threads to read concurrently without added latency of writing to the registry.
 *
 * @param <T> type of values contained in the registry.
 */
public interface Registry<T> {

    /**
     * Unique key of the registry.
     *
     * @return unique key of the registry.
     */
    @NotNull
    Key registryKey();

    /**
     * Register a new value to this registry.
     *
     * @param key   unique key of the value.
     * @param value the value.
     * @throws IllegalArgumentException if a value was already registered with the specified key,
     *                                  or that the same instance of the value is already registered.
     * @throws IllegalStateException    if the registry has been finalized.
     */
    void register(@NotNull Key key, @NotNull T value);

    /**
     * Close the registry, this will make the registry immutable.
     * <br>
     * You won't be able to register entries after this call.
     *
     * @throws IllegalStateException if the registry has already been finalized, or that the registry doesn't allow freezing.
     */
    void freezeRegistry();

    /**
     * Checks whether this registry has been frozen. (aka immutable)
     * <br><br>
     * Depending on the implementation, a frozen registry can be unfrozen.
     *
     * @return true if this registry has been frozen, false otherwise.
     */
    boolean frozen();

    /**
     * Checks whether this registry contains the specified key.
     *
     * @param key key to check for.
     * @return true if this registry contains the key, false otherwise.
     */
    boolean containsKey(@NotNull Key key);

    /**
     * Checks whether this registry contains the specified value.
     *
     * @param value value to check for.
     * @return true if this registry contains the key, false otherwise.
     */
    boolean containsValue(@NotNull T value);

    /**
     * Checks whether this registry is empty.
     *
     * @return true if this registry is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Retrieve the value from this registry.
     *
     * @param key key of the value.
     * @return optional containing the value, empty optional if no value with the key is present.
     */
    Optional<T> retrieveValue(@NotNull Key key);

    /**
     * Retrieve the key from this registry.
     *
     * @param value value attached to the key to look for.
     * @return optional containing the key, empty optional if no key could be found with the value.
     */
    Optional<Key> retrieveKey(@NotNull T value);

    /**
     * Retrieve the entry from this registry.
     * <br><br>
     * This method will always return a RegistryObject, even if the key is not present in the registry.
     * <br>
     * If the entry is not present in the registry, the RegistryObject will contain a null value.
     * <br><br>
     * This should only be used to offer static fields over entries present in the registry.
     *
     * @param key key of the entry to retrieve.
     *
     * @return registry object containing the entry, or a registry object with a null value if no entry with the key is present.
     */
    @NotNull RegistryHolder<T> retrieveHolder(@NotNull Key key);

    /**
     * Retrieve all values in this registry.
     *
     * @return an <b>immutable</b> set of all values in this registry.
     */
    @NotNull
    Set<T> values();

    /**
     * Retrieve all keys in this registry.
     *
     * @return an <b>immutable</b> set of all keys in this registry.
     */
    @NotNull
    Set<Key> keys();

    /**
     * Retrieve all entries of this registry.
     *
     * @return an <b>immutable</b> set of all entries in this registry.
     */
    Set<Map.Entry<Key, T>> entries();
}
