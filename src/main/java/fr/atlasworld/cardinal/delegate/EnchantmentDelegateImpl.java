package fr.atlasworld.cardinal.delegate;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.delegate.EnchantmentDelegate;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.EnchantmentList;
import net.minestom.server.item.enchant.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EnchantmentDelegateImpl implements EnchantmentDelegate {
    @Override
    public Map<CardinalEnchantment, Integer> enchantments(@NotNull ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null!");

        if (stack.isAir())
            return Map.of();

        EnchantmentList enchants = stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null)
            return Map.of();

        return enchants.enchantments().entrySet().stream()
                .map(entry -> {
                    Key key = entry.getKey().key();
                    CardinalEnchantment enchantment = CardinalRegistries.ENCHANTMENTS.retrieveValue(key).orElse(null);
                    if (enchantment == null)
                        return null;

                    return Map.entry(enchantment, entry.getValue());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public net.minestom.server.registry.RegistryKey<@NotNull Enchantment> enchantment(@NotNull CardinalEnchantment enchantment) {
        Preconditions.checkNotNull(enchantment, "Enchantment cannot be null!");
        return CardinalRegistries.ENCHANTMENTS.retrieveEnchantment(enchantment).orElseThrow(() -> new IllegalArgumentException("Enchantment is not registered!"));
    }
}
