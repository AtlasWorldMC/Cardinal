package fr.atlasworld.cardinal.data.source;

import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.api.exception.data.DataLoadingException;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public final class EmbeddedResourceSource implements ResourceSource {
    private final ClassLoader loader;
    private final Datapack pack;

    private final String entry;

    public EmbeddedResourceSource(ClassLoader loader, Datapack pack, String entry) {
        this.loader = loader;
        this.pack = pack;

        this.entry = entry;
    }

    @Override
    public @NotNull Datapack sourceDatapack() {
        return this.pack;
    }

    @Override
    public @NotNull InputStream openStream() throws DataLoadingException {
        try {
            InputStream stream = this.loader.getResourceAsStream(this.entry);
            if (stream == null)
                throw new FileNotFoundException("Resource not found: " + this.entry);

            return stream;
        } catch (IOException ex) {
            throw new DataLoadingException("Failed to load resource: " + this.entry, ex);
        }
    }
}
