package fr.atlasworld.cardinal.data.source;

import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.api.exception.data.DataLoadingException;
import fr.atlasworld.cardinal.data.DatapackImpl;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

public final class PackResourceSource implements ResourceSource {
    private final ZipEntry entry;
    private final DatapackImpl pack;

    public PackResourceSource(ZipEntry entry, DatapackImpl pack) {
        this.entry = entry;
        this.pack = pack;
    }

    @Override
    public @NotNull Datapack sourceDatapack() {
        return this.pack;
    }

    @Override
    public @NotNull InputStream openStream() throws DataLoadingException {
        try {
            InputStream stream = this.pack.openStream(this.entry);
            if (stream == null)
                throw new FileNotFoundException("Resource not found: " + this.entry.getName());

            return stream;
        } catch (IOException ex) {
            throw new DataLoadingException("Failed to load resource: " + this.entry.getName(), ex);
        }
    }
}
