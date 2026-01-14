package fr.atlasworld.cardinal.api.plugin;

import fr.atlasworld.cardinal.api.CardinalPlugin;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.data.Meta;
import fr.atlasworld.cardinal.api.util.KeyableNamespaced;
import net.kyori.adventure.key.KeyPattern;
import org.jetbrains.annotations.NotNull;

/**
 * Plugin running on Cardinal, use {@link CardinalPlugin} for your plugins
 */
public interface Plugin extends Datapack, Meta, KeyableNamespaced {

    /**
     * Retrieve the identifier of the plugin.
     *
     * @return plugin identifier
     */
    @Override
    @NotNull @KeyPattern.Namespace String namespace();
}
