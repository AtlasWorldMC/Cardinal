package fr.atlasworld.cardinal.api.data.gen.provider;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.data.gen.DataBus;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.CardinalDataComponents;
import fr.atlasworld.cardinal.api.server.CardinalItemProviders;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.server.item.ItemPrototype;
import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.EquipmentSlotGroup;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.AttributeList;
import net.minestom.server.item.component.Weapon;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * {@link ItemPrototype} data provider, used to define items through data packs.
 */
public abstract class ItemPrototypeDataProvider extends CodecDataProvider<ItemPrototype> {
    public static final Key ATTACK_SPEED_MODIFIER_KEY = Key.key("base_attack_speed");
    public static final Key ATTACK_DAMAGE_MODIFIER_KEY = Key.key("base_attack_damage");

    @Override
    public @NotNull Type type() {
        return Type.DATA;
    }

    @Override
    public @NotNull String path() {
        return "item";
    }

    @Override
    public final @NotNull Codec<ItemPrototype> codec() {
        return ItemPrototype.CODEC;
    }

    /**
     * Create a basic item.
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     */
    public void basicItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item) throws IOException {
        this.basicItem(bus, item, builder -> {});
    }

    /**
     * Create a basic item.
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param builder builder used to set the settings of the item.
     *
     * @throws IOException if the item could not be written to the bus.
     */
    public void basicItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, @NotNull Consumer<ItemPrototype.Builder> builder) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(item, "Item cannot be null!");
        Preconditions.checkNotNull(builder, "Builder cannot be null!");

        ItemPrototype.Builder protoBuilder = new ItemPrototype.Builder();
        builder.accept(protoBuilder);

        this.write(bus, item.key(), protoBuilder.build());
    }

    /**
     * Create a new block item (an item which can place a block).
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param block block the item should place.
     *
     * @throws IOException if the item could not be written to the bus.
     */
    public void blockItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, @NotNull RegistryHolder<CardinalBlock> block) throws IOException {
        this.blockItem(bus, item, block, builder -> {});
    }

    /**
     * Create a new block item (an item which can place a block).
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param block block the item should place.
     * @param builder builder used to set the settings of the item.
     *
     * @throws IOException if the item could not be written to the bus.
     */
    public void blockItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, @NotNull RegistryHolder<CardinalBlock> block, @NotNull Consumer<ItemPrototype.Builder> builder) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(item, "Item cannot be null!");
        Preconditions.checkNotNull(block, "Block cannot be null!");
        Preconditions.checkNotNull(builder, "Builder cannot be null!");

        this.basicItem(bus, item, builder.andThen(itemBuilder -> itemBuilder
                .material(Material.STONE).provider(CardinalItemProviders.BLOCK)
                .component(CardinalDataComponents.BLOCK_ITEM_BLOCK, block.key()))
        );
    }

    /**
     * Create a new equipment item.
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param maxDurability max durability of the item.
     * @param repairCost the repair cost of the item.
     *
     * @throws IOException if the item could not be written to the bus.
     */
    public void equipmentItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, int maxDurability, int repairCost) throws IOException {
        this.equipmentItem(bus, item, maxDurability, repairCost, builder -> {});
    }

    /**
     * Create a new equipment item.
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param maxDurability max durability of the item.
     * @param repairCost the repair cost of the item.
     * @param builder builder used to set the settings of the item.
     *
     * @throws IOException if the item could not be written to the bus.
     */
    public void equipmentItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, int maxDurability, int repairCost, @NotNull Consumer<ItemPrototype.Builder> builder) throws IOException {
        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(item, "Key cannot be null!");
        Preconditions.checkNotNull(builder, "Builder cannot be null!");

        this.basicItem(bus, item, builder.andThen(itemBuilder -> itemBuilder.maxStackSize(1)
                .component(DataComponents.MAX_DAMAGE, maxDurability)
                .component(DataComponents.REPAIR_COST, repairCost)
        ));
    }

    /**
     * Create a new weapon item.
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param maxDurability max durability of the item.
     * @param repairCost repair cost of the item.
     * @param attackDamage additional attack damage of the item. (Added on top of the user's damage attributes).
     * @param attackSpeed additional attack speed of the item. (Added on top of the user's attack speed attributes).
     * @param disableShieldForSec for how long (in seconds) this weapon disables shields.
     * @param durabilityPerAttack durability to take of the item after each attack.
     *
     * @throws IOException if the weapon could not be created.
     */

    public void weaponItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, int maxDurability, int repairCost,
                           double attackDamage, double attackSpeed, float disableShieldForSec, int durabilityPerAttack) throws IOException {
        this.weaponItem(bus, item, maxDurability, repairCost, attackDamage, attackSpeed, disableShieldForSec, durabilityPerAttack, builder -> {});
    }

    /**
     * Create a new weapon item.
     *
     * @param bus data bus.
     * @param item item for which the prototype should be created.
     * @param maxDurability max durability of the item.
     * @param repairCost repair cost of the item.
     * @param attackDamage additional attack damage of the item. (Added on top of the user's damage attributes).
     * @param attackSpeed additional attack speed of the item. (Added on top of the user's attack speed attributes).
     * @param disableShieldForSec for how long (in seconds) this weapon disables shields.
     * @param durabilityPerAttack durability to take of the item after each attack.
     * @param builder builder used to set the settings of the item.
     *
     * @throws IOException if the weapon could not be created.
     */
    public void weaponItem(@NotNull DataBus bus, @NotNull RegistryHolder<CardinalItem> item, int maxDurability, int repairCost,
                           double attackDamage, double attackSpeed, float disableShieldForSec, int durabilityPerAttack,
                           @NotNull Consumer<ItemPrototype.Builder> builder) throws IOException {

        Preconditions.checkNotNull(bus, "Bus cannot be null!");
        Preconditions.checkNotNull(item, "Item cannot be null!");
        Preconditions.checkNotNull(builder, "Builder cannot be null!");

        AttributeList.Modifier attackModifier = new AttributeList.Modifier(Attribute.ATTACK_DAMAGE,
                new AttributeModifier(ATTACK_DAMAGE_MODIFIER_KEY, attackDamage, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND);
        AttributeList.Modifier speedModifier = new AttributeList.Modifier(Attribute.ATTACK_SPEED,
                new AttributeModifier(ATTACK_SPEED_MODIFIER_KEY, attackSpeed, AttributeOperation.ADD_VALUE), EquipmentSlotGroup.MAIN_HAND);

        Weapon weapon = new Weapon(durabilityPerAttack, disableShieldForSec);
        this.equipmentItem(bus, item, maxDurability, repairCost, builder.andThen(itemBuilder -> itemBuilder
                .provider(CardinalItemProviders.WEAPON)
                .attributeModifier(modifiers -> {
                    modifiers.add(attackModifier);
                    modifiers.add(speedModifier);
                })
                .component(DataComponents.WEAPON, weapon)
        ));
    }
}
