package fr.atlasworld.cardinal.registry;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.item.enchant.Enchantment;
import net.minestom.server.registry.DynamicRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EnchantmentRegistry extends CardinalRegistry<CardinalEnchantment> {
    private final DynamicRegistry<@NotNull Enchantment> enchantmentRegistry;

    public EnchantmentRegistry(@NotNull RegistryKey key) {
        super(key);
        this.enchantmentRegistry = MinecraftServer.getEnchantmentRegistry();
    }

    @Override
    public void register(@NotNull Key key, @NotNull CardinalEnchantment value) {
        super.register(key, value);

        Enchantment enchantment = Enchantment.builder()
                .description(Component.translatable("enchantment." + key.namespace() + "." + key.value()))
                .maxLevel(value.maxLevel())
                .weight(value.weight())
                .slots(value.slots())
                .maxCost(value.maxCost())
                .minCost(value.minCost())
                .anvilCost(value.anvilCost())
                .build();

        this.enchantmentRegistry.register(key, enchantment);
    }

    public Optional<net.minestom.server.registry.RegistryKey<@NotNull Enchantment>> retrieveEnchantment(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        return Optional.ofNullable(this.enchantmentRegistry.getKey(key));
    }

    public Optional<net.minestom.server.registry.RegistryKey<@NotNull Enchantment>> retrieveEnchantment(@NotNull CardinalEnchantment enchantment) {
        Preconditions.checkNotNull(enchantment, "Enchantment cannot be null");
        Optional<Key> key = this.retrieveKey(enchantment);

        if (key.isEmpty())
            return Optional.empty();

        return this.retrieveEnchantment(key.get());
    }
}
