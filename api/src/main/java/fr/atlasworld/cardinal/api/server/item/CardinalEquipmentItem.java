package fr.atlasworld.cardinal.api.server.item;

import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Generic equipment item implementation, which handles item damages and enchantments.
 */
public class CardinalEquipmentItem extends CardinalItem {
    public CardinalEquipmentItem(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom) {
        super(base, prototype, custom);
    }

    /**
     * Retrieve the maximum durability of the item.
     *
     * @return maximum durability of the item.
     */
    public final int maxDurability() {
        return this.get(DataComponents.MAX_DAMAGE, 0);
    }

    /**
     * Retrieve the repair cost of the item.
     *
     * @return repair cost of the item.
     */
    public final int repairCost() {
        return this.get(DataComponents.REPAIR_COST, 0);
    }

    /**
     * Checks whether the item can be repaired with the given ingredient.
     *
     * @param stack                the stack to be repaired.
     * @param repairIngredient     the stack with the repair ingredient.
     * @param repairIngredientItem the {@link CardinalItem} of {@code repairIngredient}.
     * @return {@code true} if the item can be repaired, {@code false} otherwise.
     */
    public boolean canItemRepair(@NotNull ItemStack stack, @NotNull ItemStack repairIngredient, @NotNull CardinalItem repairIngredientItem) {
        return false;
    }

    /**
     * Whether the item can be broken or not.
     *
     * @return {@code true} if the item can be broken, {@code false} otherwise.
     */
    public boolean breakable() {
        return this.maxDurability() > 0 && !this.has(DataComponents.UNBREAKABLE);
    }

    /**
     * {@inheritDoc}
     * <br><br>
     * External damage cannot damage basic tools and equipments, only by usage.
     * <br>
     * Subclasses for armors and special tools should override this method to change this behavior.
     *
     * @param source {@inheritDoc}
     * @param entity {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean shouldDamage(Damage source, Entity entity) {
        if (entity instanceof ItemEntity)
            return super.shouldDamage(source, entity);

        return false;
    }
}
