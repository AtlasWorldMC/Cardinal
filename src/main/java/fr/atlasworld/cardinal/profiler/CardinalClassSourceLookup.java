package fr.atlasworld.cardinal.profiler;

import fr.atlasworld.cardinal.CardinalServer;
import fr.atlasworld.cardinal.plugin.PluginClassLoader;
import me.lucko.spark.common.sampler.source.ClassSourceLookup;

/**
 * Special implementation of the {@link ClassSourceLookup} that takes into account cardinal's plugin system.
 */
public final class CardinalClassSourceLookup implements ClassSourceLookup {
    @Override
    public String identify(Class<?> aClass) throws Exception { // Nullable
        ClassLoader classLoader = aClass.getClassLoader();
        if (classLoader instanceof PluginClassLoader pluginClassLoader)
            return pluginClassLoader.meta().identifier();

        if (classLoader == CardinalServer.class.getClassLoader())
            return "Cardinal";

        return null;
    }
}
