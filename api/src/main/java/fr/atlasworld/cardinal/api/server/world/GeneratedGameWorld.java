package fr.atlasworld.cardinal.api.server.world;

import com.google.common.base.Preconditions;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Specialized {@link GameWorld} for custom world generation.
 */
public abstract class GeneratedGameWorld implements GameWorld {

    @Override
    public final boolean generated() {
        return true;
    }

    @Override
    public final void provide(@NotNull InstanceContainer instance, @Nullable CompoundBinaryTag params) {
        Preconditions.checkNotNull(instance, "Instance cannot be null!");

        Generator generator = this.provideGenerator(params);
        Preconditions.checkNotNull(generator, "Generator cannot be null!");

        instance.setGenerator(generator);
    }

    /**
     * Called when a world is created and needs a generator.
     *
     * @param params generator parameters.
     * @return newly created generator.
     */
    @NotNull
    protected abstract Generator provideGenerator(@Nullable CompoundBinaryTag params);
}
