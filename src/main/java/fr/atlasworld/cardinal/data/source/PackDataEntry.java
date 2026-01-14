package fr.atlasworld.cardinal.data.source;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import fr.atlasworld.cardinal.data.DatapackImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.zip.ZipEntry;

public final class PackDataEntry extends AbstractDataEntry {
    private final PackResourceSource source;

    public PackDataEntry(@NotNull DatapackImpl datapack, @NotNull ZipEntry entry) throws IOException {
        super(entry.getName());

        Preconditions.checkNotNull(datapack, "Datapack cannot be null!");
        Preconditions.checkNotNull(entry, "Entry cannot be null!");
        this.source = new PackResourceSource(entry, datapack);
    }

    @Override
    public @NotNull ResourceSource asSource() {
        return this.source;
    }
}
