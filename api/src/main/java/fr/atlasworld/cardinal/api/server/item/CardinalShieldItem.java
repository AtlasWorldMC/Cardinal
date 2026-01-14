package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.entity.CardinalEntity;
import fr.atlasworld.cardinal.api.util.MathUtils;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.BlocksAttacks;
import net.minestom.server.item.component.Weapon;
import net.minestom.server.registry.RegistryTag;
import net.minestom.server.registry.TagKey;
import net.minestom.server.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Specialized shield item, this shield item has vanilla parity.
 * <br><br>
 * For customs shields that have a completely different behavior, extend the {@link CardinalEquipmentItem} class
 * and override the same methods as this class.
 */
public class CardinalShieldItem extends CardinalEquipmentItem {
    public CardinalShieldItem(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom) {
        super(base, prototype, custom);

        Preconditions.checkArgument(prototype.has(DataComponents.BLOCKS_ATTACKS), "Missing '%s' component in item prototype.", DataComponents.BLOCKS_ATTACKS.key());
    }

    /**
     * Retrieve the minimum time (in ticks) required before the shield can be used to block attacks.
     *
     * @return the minimum time (in ticks).
     */
    public float blockDelay() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        return blocks == null ? 0 : blocks.blockDelaySeconds() * 20;
    }

    /**
     * Scale used to determine the disabled time of the shield after being attacked by an item with the component
     * {@link Weapon#disableBlockingForSeconds()}.
     * <br><br>
     * This is value is multiplied by that value, and you can prevent the shield from being disabled by setting it to {@code 0}.
     *
     * @return the block cooldown scale.
     */
    public float disableCooldownScale() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        return blocks == null ? 0 : blocks.disableCooldownScale();
    }

    /**
     * Retrieve the damage types which bypass the shield.
     *
     * @return the damage tag which bypasses the shield.
     */
    public RegistryTag<@NotNull DamageType> bypassDamageTypes() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        if (blocks == null)
            return RegistryTag.empty();

        TagKey<@NotNull DamageType> tag = blocks.bypassedBy();
        if (tag == null)
            return RegistryTag.empty();

        RegistryTag<@NotNull DamageType> registryTag = MinecraftServer.getDamageTypeRegistry().getTag(tag);
        return registryTag == null ? RegistryTag.empty() : registryTag;
    }

    /**
     * Retrieve the item damage function of the shield.
     *
     * @return item damage function.
     */
    public BlocksAttacks.ItemDamageFunction itemDamageFunction() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        return blocks == null ? BlocksAttacks.ItemDamageFunction.DEFAULT : blocks.itemDamage();
    }

    /**
     * Retrieve the damage reductions of the shield.
     *
     * @return damage reductions of the shield.
     */
    public List<BlocksAttacks.DamageReduction> damageReductions() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        return blocks == null ? List.of() : blocks.damageReductions();
    }

    /**
     * Retrieve the sound to play when the shield blocks an attack.
     *
     * @return sound to play.
     */
    public SoundEvent blockSound() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        return blocks == null ? SoundEvent.ITEM_SHIELD_BLOCK : blocks.blockSound();
    }

    /**
     * Retrieve the sound to play when the shield is disabled.
     *
     * @return sound to play.
     */
    public SoundEvent disableSound() {
        BlocksAttacks blocks = this.prototype.get(DataComponents.BLOCKS_ATTACKS);
        return blocks == null ? SoundEvent.ITEM_SHIELD_BREAK : blocks.disableSound();
    }

    @Override
    public boolean canBlockDamage(@NotNull Damage source, @NotNull LivingEntity user) {
        if (this.bypassDamageTypes().contains(source.getType()))
            return false;

        // TODO: Find a way of normal entities to have the minimum block delay.
        if (!(user instanceof Player player))
            return true;

        long usingTime = player.getCurrentItemUseTime();
        return usingTime >= this.blockDelay();
    }

    @Override
    public final float blockDamage(@NotNull LivingEntity user, @NotNull PlayerHand hand, @NotNull ItemStack stack, @NotNull Damage source) {
        if (!canBlockDamage(source, user) || source.getAmount() <= 0.0F || this.damageReductions().isEmpty())
            return 0.0F;

        double angle;
        Point attackerPos = source.getSourcePosition();
        if (attackerPos != null) {
            Pos entityPos = user.getPosition();
            Vec rotation = entityPos.direction().withY(0);
            Vec difference = attackerPos.sub(entityPos).withY(0).asVec().normalize();

            angle = Math.acos(difference.dot(rotation));
        } else {
            angle = (float) Math.PI;
        }

        float damageReduction = 0.0F;
        for (BlocksAttacks.DamageReduction reduction : this.damageReductions()) {
            if (angle > (float) (Math.PI / 180) * reduction.horizontalBlockingAngle())
                continue;

            if (reduction.type() != null) {
                if (!reduction.type().contains(source.getType()))
                    continue;
            }

            damageReduction += MathUtils.clamp(reduction.base() + reduction.factor() * source.getAmount(), 0, source.getAmount());
        }

        damageReduction = CardinalEnchantment.bulkHandle(damageReduction, stack, (enchantment, level, value) ->
                enchantment.applyBlockingDamageModifier(value, level, source, stack, user, hand));

        return damageReduction;
    }

    @Override
    public void onBlock(@NotNull LivingEntity user, @NotNull PlayerHand hand, @NotNull ItemStack stack, @NotNull Damage source, float blockedAmount) {
        if (blockedAmount > 0.0F && this.blockSound() != null)
            user.getInstance().playSound(Sound.sound(this.blockSound(), CardinalEntity.retrieveSoundSource(user), 1, 0.8F + MathUtils.RANDOM.nextFloat() * 0.4F), user.getPosition());

        Entity attackerEntity = source.getAttacker();
        if (attackerEntity instanceof LivingEntity attacker && user instanceof CardinalEntity cardinalEntity) {
            ItemStack weapon = attacker.getItemInMainHand();

            CardinalItem.fromStack(weapon).ifPresent(item -> {
                float disabledTime = item.disableShields(attacker, user, weapon, stack, source) * this.disableCooldownScale();
                if (disabledTime > 0.0F) {
                    if (user instanceof Player player)
                        player.clearItemUse();

                    cardinalEntity.cooldownManager().setCooldown(stack, Math.round(disabledTime));
                    if (this.disableSound() != null)
                        user.getInstance().playSound(Sound.sound(this.disableSound(), CardinalEntity.retrieveSoundSource(user), 1, 0.8F + MathUtils.RANDOM.nextFloat() * 0.4F), user.getPosition());
                }
            });
        }

        this.damageShield(user, stack, hand, source, blockedAmount);
        super.onBlock(user, hand, stack, source, blockedAmount); // Call enchantments
    }

    /**
     * Called when the shield blocks damage and should take durability damage.
     *
     * @param user           user using the shield.
     * @param stack          stack of the shield.
     * @param hand           the hand the user holds the shield in.
     * @param source         damage source.
     * @param blockedDamages the amount of damage blocked.
     */
    public void damageShield(@NotNull LivingEntity user, @NotNull ItemStack stack, @NotNull PlayerHand hand,
                             @NotNull Damage source, float blockedDamages) {
        BlocksAttacks.ItemDamageFunction function = this.itemDamageFunction();

        if (blockedDamages < function.threshold())
            return;

        int itemDamage = MathUtils.floor(function.base() + (function.factor() * blockedDamages));
        user.setItemInHand(hand, stack.damage(itemDamage));
    }
}
