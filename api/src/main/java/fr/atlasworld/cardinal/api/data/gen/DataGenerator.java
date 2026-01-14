package fr.atlasworld.cardinal.api.data.gen;

import org.jetbrains.annotations.NotNull;

/**
 * Special class, used as entry point for data generation.
 * <br><br>
 * This is purely used for data generation and should not be used outside its scope.
 * Many api's aren't available and this mode should purely be used to generate content.
 */
public interface DataGenerator {

    /**
     * Called when the data generator starts and registers the data providers.
     *
     * @param bus bus where you should register your data.
     */
    void registerProviders(@NotNull DataBus bus);
}
