package fr.atlasworld.cardinal.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minestom.server.codec.Codec;

public final class Serializers {
    private Serializers() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final Gson GSON_PRETTY_WRITING = new GsonBuilder().setPrettyPrinting().create();
    public static final Gson GSON = new Gson();

    public static final ANSIComponentSerializer ANSI = ANSIComponentSerializer.builder().build();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    public static final PlainTextComponentSerializer PLAIN_TEXT = PlainTextComponentSerializer.builder().build();

    // Codecs
    public static final Codec<Component> MINI_MESSAGE_CODEC = Codec.STRING.transform(MINI_MESSAGE::deserialize, MINI_MESSAGE::serialize);
    public static final Codec<Component> PLAIN_TEXT_CODEC = Codec.STRING.transform(PLAIN_TEXT::deserialize, PLAIN_TEXT::serialize);
}
