package fr.atlasworld.cardinal.api.plugin.internal;

import fr.atlasworld.cardinal.api.data.DataSource;
import fr.atlasworld.cardinal.api.data.Meta;
import fr.atlasworld.cardinal.api.plugin.PluginContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;


/**
 * <b>Internal Class:</b> plugin classloader.
 */
@ApiStatus.Internal
public interface PluginLoader {
    @NotNull PluginContext context();

    @NotNull Meta meta();

    @UnknownNullability
    DataSource dataSource();
}
