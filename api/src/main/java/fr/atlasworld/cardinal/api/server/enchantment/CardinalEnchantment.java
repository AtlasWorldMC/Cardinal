package fr.atlasworld.cardinal.api.server.enchantment;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents a cardinal enchantment.
 * <br><br>
 * Enchantments are an abstract representation, depending on the systems implementing it parameters such as costs, weight, and other factors may not be used.
 * <br>
 * This also includes things such as max levels or supported items these may be completely ignored depending on the system using it.
 */
public class CardinalEnchantment {
    protected final int maxLevel;
    protected final int weight;
    protected final int anvilCost;
    protected final List<EquipmentSlotGroup> slots;
    protected final Enchantment.Cost minCost;
    protected final Enchantment.Cost maxCost;

    /**
     * Apply the current enchantment to the provided item stack.
     *
     * @param stack item stack to which the enchantment will be applied.
     * @param level level of the enchantment.
     * @return a new item stack with the enchantment applied.
     * @throws IllegalArgumentException if the level is lower than 1 or greater than the max level of the enchantment.
     */
    public final ItemStack applyEnchantment(@NotNull ItemStack stack, int level) {
        return this.applyEnchantment(stack, level, false);
    }

    /**
     * Retrieve all enchantments on the provided item stack.
     *
     * @param stack stack to retrieve enchantments from.
     * @return a map containing all enchantments on the stack mapped to their level.
     */
    public static Map<CardinalEnchantment, Integer> getEnchantments(@NotNull ItemStack stack) {
        return DelegateFactory.enchantmentDelegate().enchantments(stack);
    }

    /**
     * Helper method used to call enchantments function for all enchantments on the item.
     *
     * @param baseValue base value that will be modified, may not need to be specified if the {@link EnchantmentModifier} doesn't use it.
     * @param stack     the stack to retrieve the enchantments from.
     * @param modifier  function to apply to the modification to the enchantment.
     * @return modified value.
     */
    public static float bulkHandle(float baseValue, @NotNull ItemStack stack, @NotNull EnchantmentModifier modifier) {
        Preconditions.checkNotNull(modifier, "Modifier cannot be null!");
        Preconditions.checkNotNull(stack, "ItemStack cannot be null!");
        if (stack.isAir())
            return baseValue;

        Map<CardinalEnchantment, Integer> enchantments = getEnchantments(stack);
        if (enchantments.isEmpty())
            return baseValue;

        EnchantmentValue value = new EnchantmentValue(baseValue);
        for (Map.Entry<CardinalEnchantment, Integer> entry : enchantments.entrySet()) {
            modifier.apply(entry.getKey(), entry.getValue(), value);
        }

        return value.getValue();
    }

    /**
     * Apply the current enchantment to the provided item stack.
     *
     * @param stack          item stack to which the enchantment will be applied.
     * @param level          level of the enchantment.
     * @param bypassMaxLevel {@code true} to bypass the max level check, {@code false} otherwise.
     * @return a new item stack with the enchantment applied.
     * @throws IllegalArgumentException if the level is lower than 1, if {@code bypassMaxLevel} is {@code true} and the level is greater than the max level of the enchantment.
     */
    public final @NotNull ItemStack applyEnchantment(@NotNull ItemStack stack, int level, boolean bypassMaxLevel) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null!");
        Preconditions.checkArgument(level > 0, "Level must be greater than 0");

        if (!bypassMaxLevel && level > this.maxLevel)
            throw new IllegalArgumentException("Level must be less than or equal to the max level of the enchantment!");

        EnchantmentList list = stack.get(DataComponents.ENCHANTMENTS, EnchantmentList.EMPTY);
        list = list.with(this.asEnchantment(), level);
        return stack.with(DataComponents.ENCHANTMENTS, list);
    }

    /**
     * Create a new {@link CardinalEnchantment} with the provided properties.
     *
     * @param properties properties of the enchantment.
     */
    public CardinalEnchantment(@NotNull EnchantmentProperties properties) {
        Preconditions.checkNotNull(properties, "Enchantment properties cannot be null!");

        this.maxLevel = properties.maxLevel;
        this.weight = properties.weight;
        this.anvilCost = properties.anvilCost;
        this.slots = List.copyOf(properties.slots);
        this.minCost = properties.minCost;
        this.maxCost = properties.maxCost;
    }

    /**
     * Retrieves the maximum level of the enchantment.
     *
     * @return the maximum level as an integer
     */
    public final int maxLevel() {
        return this.maxLevel;
    }

    /**
     * Retrieves the enchantment's weight, which determines
     * how commonly it may appear in certain loot or enchanting scenarios.
     *
     * @return the weight value of the enchantment
     */
    public final int weight() {
        return this.weight;
    }

    /**
     * Retrieves the list of equipment slot groups associated with this enchantment.
     *
     * @return a list of equipment slot groups that are compatible with this enchantment.
     */
    public final List<EquipmentSlotGroup> slots() {
        return this.slots;
    }

    /**
     * Retrieves the minimum cost required for this enchantment.
     *
     * @return the minimum cost associated with this enchantment as an instance of {@link Enchantment.Cost}.
     */
    public final Enchantment.Cost minCost() {
        return this.minCost;
    }

    /**
     * Retrieves the maximum cost associated with this enchantment.
     *
     * @return the maximum cost represented as an instance of {@link Enchantment.Cost}.
     */
    public final Enchantment.Cost maxCost() {
        return this.maxCost;
    }

    /**
     * Retrieves the anvil repair cost for this enchantment.
     *
     * @return the anvil cost as an integer.
     */
    public final int anvilCost() {
        return this.anvilCost;
    }

    /**
     * Returns the enchantment as a Minestom Enchantment.
     *
     * @return the enchantment as a Minestom Enchantment.
     * @throws IllegalArgumentException if the enchantment is not registered.
     */
    public final @NotNull RegistryKey<@NotNull Enchantment> asEnchantment() {
        return DelegateFactory.enchantmentDelegate().enchantment(this);
    }

    /**
     * Checks whether a slot is compatible with the enchantment.
     *
     * @param slot slot to check.
     * @return {@code true} if the slot is compatible with the enchantment, {@code false} otherwise.
     */
    public boolean isSlotCompatible(EquipmentSlot slot) {
        if (this.slots.isEmpty())
            return true;

        for (EquipmentSlotGroup slotGroup : this.slots) {
            if (slotGroup.contains(slot))
                return true;
        }

        return false;
    }

    /**
     * Called when the entity equips an item with this enchantment.
     *
     * @param entity    entity that equipped the item.
     * @param itemStack item stack that was equipped.
     * @param slot      slot in which the item was equipped.
     * @param level     current level of the enchantment.
     */
    public void onEquip(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, int level) {
    }

    /**
     * Called when the entity unequips an item with this enchantment.
     *
     * @param entity    entity that unequipped the item.
     * @param itemStack item stack that was unequipped.
     * @param slot      slot in which the item was unequipped.
     * @param level     current level of the enchantment.
     */
    public void onUnequip(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, int level) {
    }

    /**
     * Called after an item blocked damage with this enchantment on it.
     *
     * @param level         level of the enchantment.
     * @param source        damage source.
     * @param stack         the stack that was used to block the damage and is also the item stack that has the enchantment applied.
     * @param user          the entity using the {@code stack} to block the attack.
     * @param hand          hand the {@code user} used.
     * @param blockedDamage the amount of damage that was blocked by the attack.
     */
    public void postDamageBlocked(int level, @NotNull Damage source, @NotNull ItemStack stack, @NotNull LivingEntity user, @NotNull PlayerHand hand, float blockedDamage) {
    }

    /**
     * Called after an entity attacked another with an item with this enchantment on it.
     *
     * @param level    level of the enchantment.
     * @param attacker the entity that attacked.
     * @param target   the target of the attack.
     * @param source   source of the damage.
     * @param stack    stack used by the {@code attacker} with the enchantment applied to it.
     */
    public void postAttack(int level, @NotNull LivingEntity attacker, @NotNull LivingEntity target, @NotNull Damage source,
                           @NotNull ItemStack stack) {
    }

    /**
     * Apply a modifier to the damage that will be blocked by this item. (Like a shield enchantment)
     * <br><br>
     * This should be called at the end of {@link CardinalItem#blockDamage(LivingEntity, PlayerHand, ItemStack, Damage)}
     * before returning the blocked damage, this allows the enchantment to apply modifiers to the value.
     *
     * @param damageBlocked value, containing the damage that will be blocked.
     * @param level         level of the enchantment.
     * @param source        damage source.
     * @param stack         item the {@code user} used to block the damage,
     *                      this is also the item stack that is enchanted with this enchantment.
     * @param user          the entity using the {@code stack} to block the attack.
     * @param hand          the hand the entity used to block the attack.
     */
    public void applyBlockingDamageModifier(@NotNull EnchantmentValue damageBlocked, int level, @NotNull Damage source,
                                            @NotNull ItemStack stack, @NotNull LivingEntity user, @NotNull PlayerHand hand) {
    }

    /**
     * Apply a modifier to the damage that will be applied to the target.
     *
     * @param damage   damage value, containing the damage that will be applied.
     * @param level    level of the enchantment.
     * @param attacker entity that is attacking.
     * @param target   target of the attack.
     * @param source   damage source that will be applied.
     * @param stack    stack that the attacker is using to attack and has this enchantment applied to.
     */
    public void applyDamageModifier(@NotNull EnchantmentValue damage, int level, @NotNull LivingEntity attacker,
                                    @NotNull LivingEntity target, @NotNull Damage source, @NotNull ItemStack stack) {
    }

    /**
     * Apply a modifier to the knockback that will be applied to the target after the attack.
     *
     * @param knockback knockback value, containing the knockback that will be applied.
     * @param level     level of the enchantment.
     * @param attacker  entity that is attacking.
     * @param target    target of the attack.
     * @param source    damage source that will be applied.
     * @param stack     stack that the attacker is using to attack and has this enchantment applied to.
     */
    public void applyKnockbackModifier(@NotNull EnchantmentValue knockback, int level, @NotNull LivingEntity attacker,
                                       @NotNull LivingEntity target, @NotNull Damage source, @NotNull ItemStack stack) {
    }

    /**
     * Cardinal {@link CardinalEnchantment} properties.
     */
    public static class EnchantmentProperties {
        protected final List<EquipmentSlotGroup> slots;

        protected int maxLevel;
        protected int weight;
        protected int anvilCost;
        protected Enchantment.Cost minCost;
        protected Enchantment.Cost maxCost;

        /**
         * Create a new enchantment properties.
         */
        public EnchantmentProperties() {
            this.slots = new ArrayList<>();

            this.maxLevel = 1;
            this.weight = 1;
            this.anvilCost = 1;

            this.minCost = Enchantment.Cost.DEFAULT;
            this.maxCost = Enchantment.Cost.DEFAULT;
        }

        /**
         * Max level of the enchantment.
         * <br>
         * Note, this is not enforced.
         *
         * @param maxLevel enchantment max level.
         * @return enchantment properties.
         */
        public EnchantmentProperties maxLevel(int maxLevel) {
            Preconditions.checkArgument(maxLevel > 0 && maxLevel < 255, "Max level must be in range of [1 - 255]");

            this.maxLevel = maxLevel;
            return this;
        }

        /**
         * Weight of the enchantment, higher weight means higher chance of being picked.
         * <br>
         * Note, this is not enforced.
         *
         * @param weight enchantment weight.
         * @return enchantment properties.
         */
        public EnchantmentProperties weight(int weight) {
            Preconditions.checkArgument(weight > 0 && weight < 1024, "Weight must be in range of [1 - 1024]");

            this.weight = weight;
            return this;
        }

        /**
         * Equipment slots where the enchantment will have an effect.
         * <br>
         * Depending on the implementation of {@link CardinalEnchantment}, this may be ignored.
         * <br>
         * By default, if no equipment slots are specified, the enchantment will affect all equipment slots.
         *
         * @param slots slots.
         * @return enchantment properties.
         */
        public EnchantmentProperties slots(@NotNull EquipmentSlotGroup @NotNull ... slots) {
            Preconditions.checkNotNull(slots, "Slots cannot be null!");
            this.slots.addAll(Arrays.asList(slots));
            return this;
        }

        /**
         * Set the minimum enchantment cost for the enchantment.
         *
         * @param base        base cost.
         * @param perLevelMul cost per level multiplier.
         * @return enchantment properties.
         */
        public EnchantmentProperties minCost(int base, int perLevelMul) {
            return this.minCost(new Enchantment.Cost(base, perLevelMul));
        }

        /**
         * Set the minimum enchantment cost for the enchantment.
         *
         * @param minCost minimum cost.
         * @return enchantment properties.
         */
        public EnchantmentProperties minCost(@NotNull Enchantment.Cost minCost) {
            Preconditions.checkNotNull(minCost, "Min cost cannot be null!");
            this.minCost = minCost;
            return this;
        }

        /**
         * Sets the maximum enchantment cost for the enchantment.
         *
         * @param base        base cost.
         * @param perLevelMul cost per level multiplier.
         * @return enchantment properties.
         */
        public EnchantmentProperties maxCost(int base, int perLevelMul) {
            return this.maxCost(new Enchantment.Cost(base, perLevelMul));
        }

        /**
         * Sets the maximum enchantment cost for the enchantment.
         *
         * @param maxCost maximum cost.
         * @return enchantment properties.
         */
        public EnchantmentProperties maxCost(@NotNull Enchantment.Cost maxCost) {
            Preconditions.checkNotNull(maxCost, "Max cost cannot be null!");
            this.maxCost = maxCost;
            return this;
        }
    }

    /**
     * Simple functional interface to apply enchantment modifiers.
     */
    @FunctionalInterface
    public interface EnchantmentModifier {

        /**
         * Apply the enchantment modifier.
         *
         * @param enchantment enchantment that will modify the value.
         * @param level       level of the enchantment.
         * @param value       common value shared across all enchantments which is modified and is the result of the modifier.
         */
        void apply(@NotNull CardinalEnchantment enchantment, int level, @NotNull EnchantmentValue value);
    }
}
