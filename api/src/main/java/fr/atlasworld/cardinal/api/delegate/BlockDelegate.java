package fr.atlasworld.cardinal.api.delegate;

import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Internal delegate. Do not use this class!
 * Changes inside this class won't be documented.
 */
@ApiStatus.Internal
public interface BlockDelegate {
    @NotNull Block getBlock(@NotNull CardinalBlock block);

    Optional<CardinalBlock> getCardinalBlock(@NotNull Block block);
}
