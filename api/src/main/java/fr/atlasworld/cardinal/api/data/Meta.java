package fr.atlasworld.cardinal.api.data;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Metadata attached to {@link fr.atlasworld.cardinal.api.plugin.Plugin plugins} and {@link Datapack datapacks}.
 */
public interface Meta {
    /**
     * Retrieve the display name.
     *
     * @return the display name.
     */
    @NotNull Component name();

    /**
     * Retrieve the description.
     *
     * @return description.
     */
    @NotNull Component description();

    /**
     * Retrieve the version.
     *
     * @return version.
     */
    @NotNull String version();

    /**
     * Retrieve the authors.
     *
     * @return retrieve the authors.
     */
    @NotNull Set<Component> authors();
}
