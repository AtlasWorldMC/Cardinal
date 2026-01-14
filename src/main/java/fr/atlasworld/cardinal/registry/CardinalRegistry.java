package fr.atlasworld.cardinal.registry;

import fr.atlasworld.cardinal.api.registry.SimpleRegistry;
import fr.atlasworld.cardinal.plugin.CardinalPluginManager;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Specialized registry that incorporates plugin security features.
 * <p>
 * This version of the {@link SimpleRegistry} will prevent plugin from freezing registries.
 *
 * @param <T> type of the registry.
 */
public class CardinalRegistry<T> extends SimpleRegistry<T> {
    private static final Logger LOGGER = Logging.logger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public CardinalRegistry(@NotNull Key key) {
        super(key);
    }

    @Override
    @ApiStatus.Internal
    public void freezeRegistry() {
        if (CardinalPluginManager.isPluginClass(STACK_WALKER.getCallerClass())) {
            LOGGER.error("A plugin attempted to freeze registry '{}'.", this.key);
            return;
        }

        super.freezeRegistry();
    }
}
