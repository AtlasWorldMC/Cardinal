package fr.atlasworld.cardinal.event.entity;

import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.item.EntityEquipEvent;
import net.minestom.server.item.ItemStack;
import org.slf4j.Logger;

import java.util.Optional;

public final class EntityEquipmentHandler {
    private static final Logger LOGGER = Logging.logger();

    public static void onEquipmentChange(EntityEquipEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity))
            return;

        EquipmentSlot slot = event.getSlot();
        ItemStack newStack = event.getItemStack();
        ItemStack oldStack = entity.getEquipment(slot);

        if (!slot.isArmor())
            return;

        if (!oldStack.isAir())
            handleUnequip(entity, oldStack, slot);

        if (!newStack.isAir())
            handleEquip(entity, newStack, slot);
    }

    private static void handleEquip(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
        CardinalEnchantment.getEnchantments(stack).forEach((enchantment, level) -> {
            try {
                enchantment.onEquip(entity, stack, slot, level);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass equip event to {}:", enchantment.getClass().getSimpleName(), ex);
            }
        });

        Optional<CardinalItem> item = CardinalItem.fromStack(stack);
        item.ifPresent(oldItem -> {
            try {
                oldItem.onEquip(entity, stack, slot);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass equip event to {}:", item.getClass().getSimpleName(), ex);
            }
        });
    }

    private static void handleUnequip(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
        CardinalEnchantment.getEnchantments(stack).forEach((enchantment, level) -> {
            try {
                enchantment.onUnequip(entity, stack, slot, level);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass unequip event to {}:", enchantment.getClass().getSimpleName(), ex);
            }
        });

        Optional<CardinalItem> item = CardinalItem.fromStack(stack);
        item.ifPresent(oldItem -> {
            try {
                oldItem.onUnequip(entity, stack, slot);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass unequip event to {}:", item.getClass().getSimpleName(), ex);
            }
        });
    }
}
