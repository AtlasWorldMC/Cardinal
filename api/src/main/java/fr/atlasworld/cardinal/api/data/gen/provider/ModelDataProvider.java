package fr.atlasworld.cardinal.api.data.gen.provider;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creative.model.Model;
import team.unnamed.creative.model.ModelTexture;
import team.unnamed.creative.model.ModelTextures;
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Basic model provider, used to generate generic models, should be extended for specific models such as block or item models.
 *
 * @param <T> type of the model to create.
 */
public abstract class ModelDataProvider<T> implements DataProvider {

    @Override
    public @NotNull Type type() {
        return Type.ASSET;
    }

    @Override
    public @NotNull String path() {
        return "models";
    }

    /**
     * Generate a crop model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param texture crop texture key.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void crop(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key texture) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(texture, "Texture cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/crop"))
                .textures(ModelTextures.builder()
                        .addVariable("crop", ModelTexture.ofKey(texture))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a cross model, used for grass and plants.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param texture cross texture key.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cross(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key texture) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(texture, "Texture cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/cross"))
                .textures(ModelTextures.builder()
                        .addVariable("cross", ModelTexture.ofKey(texture))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a cube model, with one uniform texture for all faces.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param texture texture key.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cubeAll(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key texture) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(texture, "Texture cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/cube_all"))
                .textures(ModelTextures.builder()
                        .addVariable("all", ModelTexture.ofKey(texture))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a stairs model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param side side texture.
     * @param bottom bottom texture.
     * @param top top texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void stairs(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key side, @NotNull Key bottom, @NotNull Key top) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(bottom, "Bottom cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/stairs"))
                .textures(ModelTextures.builder()
                        .addVariable("side", ModelTexture.ofKey(side))
                        .addVariable("bottom", ModelTexture.ofKey(bottom))
                        .addVariable("top", ModelTexture.ofKey(top))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate an outer stairs model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param side side texture.
     * @param bottom bottom texture.
     * @param top top texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void stairsOuter(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key side, @NotNull Key bottom, @NotNull Key top) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(bottom, "Bottom cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/outer_stairs"))
                .textures(ModelTextures.builder()
                        .addVariable("side", ModelTexture.ofKey(side))
                        .addVariable("bottom", ModelTexture.ofKey(bottom))
                        .addVariable("top", ModelTexture.ofKey(top))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate an inner stairs model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param side side texture.
     * @param bottom bottom texture.
     * @param top top texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void stairsInner(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key side, @NotNull Key bottom, @NotNull Key top) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(bottom, "Bottom cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/inner_stairs"))
                .textures(ModelTextures.builder()
                        .addVariable("side", ModelTexture.ofKey(side))
                        .addVariable("bottom", ModelTexture.ofKey(bottom))
                        .addVariable("top", ModelTexture.ofKey(top))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a cube model, with a top and side texture.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param top top texture.
     * @param side side textures, including the bottom face.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cubeTop(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key top, @NotNull Key side) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");
        Preconditions.checkNotNull(side, "Side cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/cube_top"))
                .textures(ModelTextures.builder()
                        .addVariable("top", ModelTexture.ofKey(top))
                        .addVariable("side", ModelTexture.ofKey(side))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a cube model, with a bottom and top texture.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param bottom bottom face texture.
     * @param top top face texture.
     * @param side side faces textures.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cubeBottomTop(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key bottom, @NotNull Key top, @NotNull Key side) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(bottom, "Bottom cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");
        Preconditions.checkNotNull(side, "Side cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/cube_bottom_top"))
                .textures(ModelTextures.builder()
                        .addVariable("top", ModelTexture.ofKey(top))
                        .addVariable("side", ModelTexture.ofKey(side))
                        .addVariable("bottom", ModelTexture.ofKey(bottom))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a slab model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param bottom bottom face texture.
     * @param top top face texture.
     * @param side side faces textures.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void slab(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key bottom, @NotNull Key top, @NotNull Key side) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(bottom, "Bottom cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");
        Preconditions.checkNotNull(side, "Side cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/slab"))
                .textures(ModelTextures.builder()
                        .addVariable("top", ModelTexture.ofKey(top))
                        .addVariable("side", ModelTexture.ofKey(side))
                        .addVariable("bottom", ModelTexture.ofKey(bottom))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a slab top model. (Top version of the slab)
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param bottom bottom face texture.
     * @param top top face texture.
     * @param side side faces textures.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void slabTop(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key bottom, @NotNull Key top, @NotNull Key side) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(bottom, "Bottom cannot be null!");
        Preconditions.checkNotNull(top, "Top cannot be null!");
        Preconditions.checkNotNull(side, "Side cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/slab_top"))
                .textures(ModelTextures.builder()
                        .addVariable("top", ModelTexture.ofKey(top))
                        .addVariable("side", ModelTexture.ofKey(side))
                        .addVariable("bottom", ModelTexture.ofKey(bottom))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a button model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param texture texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void button(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key texture) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(texture, "Texture cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/button"))
                .textures(ModelTextures.builder()
                        .addVariable("texture", ModelTexture.ofKey(texture))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a pressed button model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param texture texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void buttonPressed(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key texture) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(texture, "Texture cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/button_pressed"))
                .textures(ModelTextures.builder()
                        .addVariable("texture", ModelTexture.ofKey(texture))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a button model used in inventories.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param texture texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void buttonInventory(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key texture) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(texture, "Texture cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/button_inventory"))
                .textures(ModelTextures.builder()
                        .addVariable("texture", ModelTexture.ofKey(texture))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    // TODO: Add pressure plate, fence (gates), Walls, Glass Pane, door, trap door, torch, carpet, leaves (..?)

    /**
     * Generate a cube column model, such as a pillar or a log.
     *
     * @param bus data bus.
     * @param element element for which this model is generated.
     * @param side side texture.
     * @param end both ends textures.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cubeColumn(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key side, @NotNull Key end) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(side, "Side cannot be null!");
        Preconditions.checkNotNull(end, "End cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/cube_column"))
                .textures(ModelTextures.builder()
                        .addVariable("end", ModelTexture.ofKey(end))
                        .addVariable("side", ModelTexture.ofKey(side))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a cube horizontal column model, such as a pillar or a log.
     *
     * @param bus data bus.
     * @param element element for which this model is generated.
     * @param side side texture.
     * @param end both ends textures.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cubeColumnHorizontal(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key side, @NotNull Key end) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(side, "Side cannot be null!");
        Preconditions.checkNotNull(end, "End cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("block/cube_column_horizontal"))
                .textures(ModelTextures.builder()
                        .addVariable("end", ModelTexture.ofKey(end))
                        .addVariable("side", ModelTexture.ofKey(side))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Generate a cube model.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param up up-facing texture.
     * @param down down-facing texture.
     * @param north north-facing texture.
     * @param south south-facing texture.
     * @param west west-facing texture.
     * @param east east-facing texture.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void cube(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Key up, @NotNull Key down,
                     @NotNull Key north, @NotNull Key south, @NotNull Key west, @NotNull Key east) throws IOException {

        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(up, "Up cannot be null!");
        Preconditions.checkNotNull(down, "Down cannot be null!");
        Preconditions.checkNotNull(north, "North cannot be null!");
        Preconditions.checkNotNull(south, "South cannot be null!");
        Preconditions.checkNotNull(west, "West cannot be null!");
        Preconditions.checkNotNull(east, "East cannot be null!");

        Model model = Model.model()
                .key(element.key())
                .parent(Key.key("cube"))
                .textures(ModelTextures.builder()
                        .addVariable("up", ModelTexture.ofKey(up))
                        .addVariable("down", ModelTexture.ofKey(down))
                        .addVariable("north", ModelTexture.ofKey(north))
                        .addVariable("south", ModelTexture.ofKey(south))
                        .addVariable("west", ModelTexture.ofKey(west))
                        .addVariable("east", ModelTexture.ofKey(east))
                        .build()
                )
                .build();

        this.write(bus, element, model);
    }

    /**
     * Write a model to the bus.
     *
     * @param bus data bus.
     * @param element element for which the model is generated.
     * @param model model to write.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void write(@NotNull DataBus bus, @NotNull RegistryHolder<T> element, @NotNull Model model) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(element, "Element cannot be null!");
        Preconditions.checkNotNull(model, "Model cannot be null!");

        try (OutputStream stream = bus.openStream(this, element.key())) {
            ModelSerializer.INSTANCE.serialize(model, stream, MinecraftServer.RESOURCE_PACK_VERSION);
        }
    }
}
