package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Weapon;
import org.jetbrains.annotations.NotNull;

public class CardinalWeaponItem extends CardinalEquipmentItem {
    public CardinalWeaponItem(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom) {
        super(base, prototype, custom);

        Preconditions.checkArgument(this.prototype().has(DataComponents.WEAPON), "Missing " + DataComponents.WEAPON.key() + " component in item prototype.");
    }

    /**
     * Retrieve the amount of damage to cause the item when hitting an entity.
     *
     * @return the amount of damage.
     */
    public int damagePerHit() {
        Weapon weapon = this.get(DataComponents.WEAPON);
        return weapon == null ? 0 : weapon.itemDamagePerAttack();
    }

    /**
     * Retrieve the time (in seconds) a shield should be disabled after being attacked by this item.
     * <br>
     * Note: this is still multiplied against {@link CardinalShieldItem#disableCooldownScale()}.
     *
     * @return the time (in seconds).
     */
    public float disableShieldTime() {
        Weapon weapon = this.get(DataComponents.WEAPON);
        return weapon == null ? 0 : weapon.disableBlockingForSeconds();
    }


    @Override
    public boolean onAttack(@NotNull LivingEntity attacker, @NotNull LivingEntity target, @NotNull Damage damage, @NotNull ItemStack stack) {
        // TODO: Implement and handle item durability.
        return super.onAttack(attacker, target, damage, stack);
    }

    @Override
    public int disableShields(@NotNull LivingEntity user, @NotNull LivingEntity target, @NotNull ItemStack weapon, @NotNull ItemStack shield, @NotNull Damage source) {
        return Math.round(this.disableShieldTime() * 20);
    }
}
