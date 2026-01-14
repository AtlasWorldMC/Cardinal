package fr.atlasworld.cardinal.api.data.gen.provider;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import team.unnamed.creative.item.Item;
import team.unnamed.creative.item.ItemModel;
import team.unnamed.creative.serialize.minecraft.item.ItemSerializer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Item model definition data provider, used in resource packs.
 */
public abstract class ItemModelDefinitionDataProvider implements DataProvider {

    @Override
    public @NotNull Type type() {
        return Type.ASSET;
    }

    @Override
    public @NotNull String path() {
        return "items";
    }

    /**
     * Create a basic item model definition, which simply references the model.
     *
     * @param bus data bus.
     * @param item item for which the model should be created.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void createItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item) throws IOException {
        this.writeItem(bus, item, Item.item(item.key(), ItemModel.reference(Key.key(item.key().namespace(), "item/" + item.key().value())), true));
    }

    /**
     * Create a basic block item model definition, which simply references the model.
     *
     * @param bus data bus.
     * @param item item for which the model should be created.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void createBlock(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item) throws IOException {
        this.writeItem(bus, item, Item.item(item.key(), ItemModel.reference(Key.key(item.key().namespace(), "block/" + item.key().value())), true));
    }

    /**
     * Create a new item model definition.
     *
     * @param bus data bus.
     * @param item item for which the model should be created.
     * @param definition the item model definition.
     *
     * @throws IOException if the model could not be written to the bus.
     */
    public void writeItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, @NotNull Item definition) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(item, "Item cannot be null!");
        Preconditions.checkNotNull(definition, "Definition cannot be null!");

        try (OutputStream stream = bus.openStream(this, item.key())) {
            ItemSerializer.INSTANCE.serialize(definition, stream, MinecraftServer.RESOURCE_PACK_VERSION);
        }
    }
}
