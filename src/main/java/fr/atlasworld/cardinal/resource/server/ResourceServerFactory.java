package fr.atlasworld.cardinal.resource.server;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.configuration.ServerConfiguration;
import fr.atlasworld.cardinal.resource.ResourceServer;
import org.jetbrains.annotations.NotNull;

public final class ResourceServerFactory {
    private ResourceServerFactory() {
        throw new UnsupportedOperationException("Factory class");
    }

    // TODO: Add configuration to choose between embedded or external web server
    public static ResourceServer createResourceServer(@NotNull ServerConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "Configuration cannot be null");

        return new EmbeddedResourceServer(configuration);
    }
}
