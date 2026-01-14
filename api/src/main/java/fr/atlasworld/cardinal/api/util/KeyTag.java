package fr.atlasworld.cardinal.api.util;

import fr.atlasworld.cardinal.api.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.tag.TagSerializer;
import net.minestom.server.tag.TagWritable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class KeyTag implements TagSerializer<@NotNull Key> {
    private static final Tag<@NotNull String> NAMESPACE_TAG = Tag.String("namespace");
    private static final Tag<@NotNull String> VALUE_TAG = Tag.String("value");

    private static final KeyTag INSTANCE = new KeyTag();

    private KeyTag() {
    }

    @NotNull
    public static Tag<@NotNull Key> create(@NotNull String key) {
        return Tag.Structure(key, INSTANCE);
    }

    @Override
    public @Nullable Key read(@NotNull TagReadable reader) {
        String namespace = reader.getTag(NAMESPACE_TAG);
        String key = reader.getTag(VALUE_TAG);

        if (!RegistryKey.isValidNamespace(namespace) || !RegistryKey.isValidKey(key))
            return null; // Invalid

        return new RegistryKey(namespace, key);
    }

    @Override
    public void write(@NotNull TagWritable writer, @NotNull Key value) {
        writer.setTag(NAMESPACE_TAG, value.namespace());
        writer.setTag(VALUE_TAG, value.value());
    }
}
