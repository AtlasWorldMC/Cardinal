package fr.atlasworld.cardinal.api.delegate;

import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@ApiStatus.Internal
public interface EnchantmentDelegate {
    Map<CardinalEnchantment, Integer> enchantments(@NotNull ItemStack stack);

    RegistryKey<@NotNull Enchantment> enchantment(@NotNull CardinalEnchantment enchantment);
}
