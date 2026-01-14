package fr.atlasworld.cardinal.api.server.item;

import net.minestom.server.component.DataComponentMap;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Interface used when loading item prototype read from a datapack, used to construct custom item instances.
 */
@FunctionalInterface
public interface ItemProvider {

    /**
     * Creates a new item instance for the specified prototype.
     *
     * @param base base material of the item.
     * @param prototype component map containing all the set components of the item.
     * @param custom whether the item should be considered custom.
     *
     * @return newly created item.
     */
    @NotNull CardinalItem build(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom);

    /**
     * Creates a new item instance for the specified prototype.
     *
     * @param prototype item prototype.
     *
     * @return newly created item.
     */
    default @NotNull CardinalItem build(@NotNull ItemPrototype prototype) {
        return this.build(prototype.material(), prototype.resolveComponents(), prototype.custom());
    }

    /**
     * Create a basic builder, if you're class doesn't change the constructor signatures of {@link CardinalItem} you can use this.
     *
     * @param itemClass class to construct.
     *
     * @return a new {@link ItemProvider} for a basic class.
     */
    static ItemProvider basicProvider(Class<? extends CardinalItem> itemClass) {
        try {
            Constructor<? extends CardinalItem> constructor = itemClass.getDeclaredConstructor(Material.class, DataComponentMap.class, boolean.class);

            return (base, prototype, customModel) -> {
                try {
                    return constructor.newInstance(base, prototype, customModel);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create item!", e);
                }
            };

        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Cannot find proper basic constructor in '" + itemClass.getName() + "', if your item class needs additional arguments, implement a proper ItemBuilder for it: ", e);
        }
    }
}
