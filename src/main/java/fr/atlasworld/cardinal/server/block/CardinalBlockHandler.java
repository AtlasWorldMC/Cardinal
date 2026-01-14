package fr.atlasworld.cardinal.server.block;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CardinalBlockHandler implements BlockHandler {
    private final Key key;
    private final CardinalBlock block;

    public CardinalBlockHandler(@NotNull Key key, @NotNull CardinalBlock block) {
        Preconditions.checkNotNull(key, "Key may not be null!");
        Preconditions.checkNotNull(block, "Block may not be null!");

        this.key = key;
        this.block = block;
    }

    @Override
    public @NotNull Key getKey() {
        return this.key;
    }

    public @NotNull CardinalBlock getBlock() {
        return this.block;
    }

    @Override
    public void onPlace(@NotNull Placement placement) {
        this.block.onPlace(placement);
    }

    @Override
    public void onDestroy(@NotNull Destroy destroy) {
        this.block.onDestroy(destroy);
    }

    @Override
    public boolean onInteract(@NotNull Interaction interaction) {
        return this.block.onInteract(interaction);
    }

    @Override
    public void onTouch(@NotNull Touch touch) {
        this.block.onTouch(touch);
    }

    @Override
    public void tick(@NotNull Tick tick) {
        this.block.tick(tick);
    }

    @Override
    public boolean isTickable() {
        return this.block.tickable();
    }

    @Override
    public @NotNull Collection<Tag<?>> getBlockEntityTags() {
        return this.block.blockEntityTags();
    }

    @Override
    public byte getBlockEntityAction() {
        return this.block.currentBlockEntityAction();
    }
}
