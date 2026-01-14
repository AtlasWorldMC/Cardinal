package fr.atlasworld.cardinal.api.data.gen.provider;

import fr.atlasworld.cardinal.api.annotation.DataGenProvider;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Data Provider, the base of all DataProviders offers the basic logic for data or asset generation.
 */
@DataGenProvider
public interface DataProvider {

    /**
     * Type of the data provider.
     *
     * @return type of the data provider.
     */
    @NotNull Type type();

    /**
     * Represents where in the data or resource pack the generator will write to.
     * <br><br>
     * <b>Example:</b> To write to the /item/ directory in the datapack this should return {@code item}.
     *
     * @return path to write the data.
     */
    @NotNull String path();

    /**
     * Called when the data provider should start generating data or assets.
     *
     * @param bus data bus.
     *
     * @throws IOException if the data generation failed.
     */
    void generate(@NotNull DataBus bus) throws IOException;

    /**
     * The type of the provider.
     */
    enum Type {
        /**
         * Asset provider; will provide data generation for resource packs.
         */
        ASSET,

        /**
         * Data provider; will provide data generation for data packs.
         */
        DATA
    }
}
