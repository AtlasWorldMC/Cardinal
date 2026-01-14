package fr.atlasworld.cardinal.data.source;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.data.ResourceSource;
import org.jetbrains.annotations.NotNull;

/**
 * DataEntry implementation for embedded datapacks inside the core program or external plugins.
 */
public final class EmbeddedDataEntry extends AbstractDataEntry {
    private final EmbeddedResourceSource source;

    public EmbeddedDataEntry(@NotNull ClassLoader loader, @NotNull Datapack datapack, @NotNull String entry) {
        super(entry);

        Preconditions.checkNotNull(loader, "Loader cannot be null!");
        Preconditions.checkNotNull(datapack, "Datapack cannot be null!");
        Preconditions.checkNotNull(entry, "Entry cannot be null!");

        this.source = new EmbeddedResourceSource(loader, datapack, entry);
    }

    @Override
    public @NotNull ResourceSource asSource() {
        return this.source;
    }
}
