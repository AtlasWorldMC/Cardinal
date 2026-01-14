package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.delegate.DelegateFactory;
import fr.atlasworld.cardinal.api.registry.RegistryHolder;
import fr.atlasworld.cardinal.api.server.block.CardinalBlock;
import fr.atlasworld.cardinal.api.server.component.ServerDataComponent;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.util.KeyTag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponent;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.item.component.ItemRarity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Represents a custom item on cardinal.
 */
public class CardinalItem implements DataComponent.Holder, ServerDataComponent.Holder {
    public static final String ITEM_TRANSLATION_KEY = "item.%s.%s";
    public static final String BLOCK_TRANSLATION_KEY = "block.%s.%s";

    public static final Tag<@NotNull Key> CARDINAL_ITEM_TAG = KeyTag.create("cardinalItem");

    /**
     * Create an item stack from a {@link CardinalItem}
     *
     * @param item item.
     * @return newly created ItemStack.
     */
    public static @NotNull ItemStack createStack(CardinalItem item) {
        return DelegateFactory.itemDelegate().createStack(item);
    }

    /**
     * Create an item stack from a {@link CardinalItem}
     *
     * @param item   item
     * @param amount stack size.
     * @return newly created ItemStack.
     */
    public static @NotNull ItemStack createStack(CardinalItem item, int amount) {
        return DelegateFactory.itemDelegate().createStack(item, amount);
    }

    /**
     * Retrieve the {@link CardinalItem} from a {@link ItemStack}.
     *
     * @param stack item stack.
     * @return optional containing the cardinal item associated with the stack, or empty if the stack is not a cardinal item.
     */
    public static Optional<CardinalItem> fromStack(ItemStack stack) {
        return DelegateFactory.itemDelegate().getItem(stack);
    }

    protected final Material base;
    protected final DataComponentMap prototype;
    protected final boolean custom;

    /**
     * Create a new cardinal item.
     */
    public CardinalItem(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom) {
        Preconditions.checkNotNull(base, "base");
        Preconditions.checkNotNull(prototype, "prototype");

        this.base = base;
        this.prototype = prototype;
        this.custom = custom;
    }

    /**
     * Builds the item stack that will be using this item.
     *
     * @param key key associated with the item.
     * @param builder the stack builder.
     */
    @SuppressWarnings("unchecked")
    public final void apply(@NotNull Key key, @NotNull ItemStack.Builder builder) {
        builder.material(this.base);

        for (DataComponent.Value value : this.prototype.entrySet()) {
            if (value.component() == DataComponents.CUSTOM_DATA && CardinalServer.isProduction())
                continue; // Prevent propagating server side data components unless in dev mode, note, this will write the custom component data to the ItemStack this should only be used to send the custom server data components to the client.

            if (value.value() == null)
                continue;

            builder.set((DataComponent<? super Object>) value.component(), value.value());
        }

        if (this.custom) {
            builder.set(DataComponents.ITEM_MODEL, key.toString());

            if (this instanceof CardinalBlockItem)
                builder.set(DataComponents.ITEM_NAME, Component.translatable(String.format(BLOCK_TRANSLATION_KEY, key.namespace(), key.value())));
            else
                builder.set(DataComponents.ITEM_NAME, Component.translatable(String.format(ITEM_TRANSLATION_KEY, key.namespace(), key.value())));
        }
    }

    /**
     * Create a new item stack with this item.
     *
     * @param amount stack size.
     * @return newly created ItemStack.
     */
    public final @NotNull ItemStack createStack(int amount) {
        return DelegateFactory.itemDelegate().createStack(this, amount);
    }

    /**
     * Create a new item stack with this item.
     *
     * @return newly created ItemStack.
     */
    public final @NotNull ItemStack createStack() {
        return DelegateFactory.itemDelegate().createStack(this);
    }

    /**
     * Retrieves the base material of the item.
     *
     * @return the base material of the item.
     */
    public final @NotNull Material base() {
        return this.base;
    }

    /**
     * Determines if the current item is flagged as custom.
     *
     * @return {@code true} if the item is custom, otherwise {@code false}.
     */
    public final boolean isCustom() {
        return this.custom;
    }

    /**
     * Prototype of this item.
     *
     * @return prototype of this item.
     */
    public final @NotNull DataComponentMap prototype() {
        return this.prototype;
    }

    /**
     * Retrieves the rarity value of the item.
     *
     * @return the {@link ItemRarity} representing the rarity of the item.
     */
    public final @NotNull ItemRarity rarity() {
        return this.get(DataComponents.RARITY, ItemRarity.COMMON);
    }

    /**
     * Retrieves the maximum stack size allowed for this item.
     *
     * @return the maximum number of items that can be stacked together.
     */
    public final int maxStackSize() {
        return this.get(DataComponents.MAX_STACK_SIZE, 64);
    }

    /**
     * Tooltip style, sets the tooltips textures sprites for the item.
     * <br><br>
     * This will make the game load the textures at:
     * <ul>
     *     <li>{@code assets/<namespace>/textures/gui/sprites/tooltip/<value>_background} which is the background of the tooltip.</li>
     *     <li>{@code assets/<namespace>/textures/gui/sprites/tooltip/<value>_frame}which is the frame of the tooltip.</li>
     * </ul>
     *
     * @return registry key to the tooltip textures.
     */
    public @Nullable Key tooltipStyle() {
        return null;
    }

    /**
     * Create a damage source to apply to a target when attacking.
     * <p>
     * This is used to determine which damage source to apply to a target.
     *
     * @param user the user using this item. (Which in this case is the attacker)
     * @return the damage source created.
     */
    public @Nullable Damage damageSource(LivingEntity user) {
        return null;
    }

    /**
     * Compute bonus attack damage to the target.
     *
     * @param attacker     the entity attacking.
     * @param target       target of the attack.
     * @param baseDamage   base damage calculated for the attack.
     * @param damageSource damage source.
     * @param stack        stack of this item, and the stack the {@code attacker} is using to damage the target.
     * @return additional damage to add to the base damage.
     */
    public float getBonusAttackDamage(@NotNull LivingEntity attacker, @NotNull LivingEntity target, float baseDamage,
                                      @NotNull Damage damageSource, @NotNull ItemStack stack) {
        return CardinalEnchantment.bulkHandle(baseDamage, stack, ((enchantment, level, value) ->
                enchantment.applyDamageModifier(value, level, attacker, target, damageSource, stack))) - baseDamage;
    }

    /**
     * Compute the bonus knockback to the target.
     *
     * @param attacker      the entity attacking.
     * @param target        the target of the attack.
     * @param baseKnockback base knockback calculated for the attack.
     * @param damageSource  damage source.
     * @param stack         stack of this item, and the stack the {@code attacker} is using to damage the target.
     * @return additional knockback to add to the base knockback.
     */
    public float getBonusKnockback(@NotNull LivingEntity attacker, @NotNull LivingEntity target, float baseKnockback,
                                   @NotNull Damage damageSource, @NotNull ItemStack stack) {
        return CardinalEnchantment.bulkHandle(baseKnockback, stack, ((enchantment, level, value) ->
                enchantment.applyKnockbackModifier(value, level, attacker, target, damageSource, stack)));
    }

    /**
     * Called when an entity used this item to attack another entity.
     *
     * @param attacker the entity that attacked the target.
     * @param target   target of the attack.
     * @param damage   damage source and caused.
     * @param stack    stack with this item.
     * @return whether to perform the mêlée attack to the target, {@code false} to prevent the action, {@code true} to allow it.
     */
    public boolean onAttack(@NotNull LivingEntity attacker, @NotNull LivingEntity target, @NotNull Damage damage, @NotNull ItemStack stack) {
        return true;
    }

    /**
     * Called when the item is getting dropped.
     *
     * @param instance  instance in which the item is getting dropped.
     * @param player    player dropping the item.
     * @param itemStack stack getting dropped.
     * @return whether the action is allowed to be performed, {@code true} to allow the item dropping, {@code false} to prevent it.
     */
    public boolean onDrop(@NotNull Instance instance, @NotNull Player player, @NotNull ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(itemStack);

        itemEntity.setMergeable(this.maxStackSize() > 1);
        itemEntity.setPickupDelay(500, ChronoUnit.MILLIS);
        itemEntity.setInstance(instance, player.getPosition().add(0, 1.5F, 0));
        itemEntity.setVelocity(player.getPosition().direction().mul(6));

        return true;
    }

    /**
     * Determines whether the item should take durability damage.
     *
     * @param source source damage.
     * @param entity this can be the {@link ItemEntity} or a {@link LivingEntity} depending on the situation,
     *               when it's an {@link ItemEntity} it means that this item in Entity form is taking damage.
     *               in the case of a {@link LivingEntity} it means that the entity is taking damage, and has this item equipped.
     * @return {@code true} if the item should take damage, {@code false} otherwise.
     */
    public boolean shouldDamage(Damage source, Entity entity) {
        return false;
    }

    /**
     * Called when the item is getting picked up.
     *
     * @param entity     entity picking up the item.
     * @param itemEntity item entity.
     * @param itemStack  the content of {@code itemEntity}.
     * @return whether the action is allowed to be performed, {@code true} to allow the entity to pick up the item, {@code false} to prevent it.
     */
    public boolean onPickup(@NotNull LivingEntity entity, @NotNull ItemEntity itemEntity, @NotNull ItemStack itemStack) {
        if (!entity.canPickupItem())
            return false;

        if (entity instanceof Player player)
            return player.getInventory().addItemStack(itemStack, TransactionOption.ALL_OR_NOTHING);

        if (!entity.getItemInMainHand().isAir())
            return false;

        entity.setItemInMainHand(itemStack);
        return true;
    }

    /**
     * Called when the item is picked by scroll-clicking on the block.
     * <br>
     * Usually this won't be called unless you are a subclass of {@link CardinalBlockItem}.
     * And that the item is registered to a {@link CardinalBlock} using {@link CardinalBlock#setItem(CardinalBlockItem)}
     *
     * @param player      player picking up the item.
     * @param stack       item stack.
     * @param block       block on which the player clicked.
     * @param blockPos    block position.
     * @param includeData whether to include data.
     * @return final item stack to send to the player, or {@code null} to not send anything.
     */
    public @Nullable ItemStack onBlockPick(@NotNull Player player, @NotNull ItemStack stack, @NotNull Block block, @NotNull Point blockPos, boolean includeData) {
        return stack;
    }

    /**
     * Called when an item is equipped.
     *
     * @param entity    entity equipping the item.
     * @param itemStack item stack.
     * @param slot      slot in which the item is equipped.
     */
    public void onEquip(@NotNull LivingEntity entity, @NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
    }

    /**
     * Called when an item is unequipped.
     *
     * @param entity    entity unequipping the item.
     * @param itemStack stack that was unequipped.
     * @param slot      slot in which the item was equipped.
     */
    public void onUnequip(@NotNull LivingEntity entity, @NotNull ItemStack itemStack, @NotNull EquipmentSlot slot) {
    }

    /**
     * Called when the item is interacted with a block.
     *
     * @param player    player that interacted.
     * @param hand      hand the player used to interact.
     * @param itemStack stack used to interact.
     * @param block     block type that was interacted with.
     * @param blockPos  block position.
     * @param cursorPos cursor position on the block.
     * @param face      face of the block the interaction happened.
     * @return the resulting action to take.
     */
    public @NotNull InteractionResult onBlockInteract(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack,
                                                      @NotNull Block block, @NotNull Point blockPos, @NotNull Point cursorPos, @NotNull BlockFace face) {
        return InteractionResult.IGNORE;
    }

    /**
     * Called when a block is broken using this item.
     *
     * @param player    player that broke the block.
     * @param itemStack stack used to break the block.
     * @param block     block destroyed.
     * @param blockPos  block position.
     * @param face      face on which the player was clicking.
     * @return the resulting action to take.
     */
    public BlockInteractionResult onBlockBreak(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull Block block,
                                               @NotNull Point blockPos, @NotNull BlockFace face) {
        return BlockInteractionResult.none();
    }

    /**
     * Called when a block is placed with this item.
     * <p>
     * <b>Warning:</b> this can only be called if your base material {@link Material#isBlock() is a block}.
     *
     * @param player    player that placed the block.
     * @param hand      hand that the player used to place the block.
     * @param itemStack stack used by the player to place the block.
     * @param block     block placed.
     * @param blockPos  block position.
     * @param face      face on which the block was placed.
     * @return the resulting action to take.
     */

    public BlockInteractionResult onBlockPlace(@NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack,
                                               @NotNull Block block, @NotNull Point blockPos, @NotNull BlockFace face) {
        return BlockInteractionResult.none();
    }

    /**
     * Sets how long (in ticks) the item is being used.
     *
     * @return the time the item can be used for, or {@code -1} if undefined.
     */
    public long usingDurationTime() {
        return -1;
    }

    /**
     * Called when a player starts using the item.
     * <br>
     * This can be eating, blocking, charging, ect...
     * <p>
     * The time the item is used is defined with {@link #usingDurationTime()}.
     *
     * @param entity    player which is using the item.
     * @param hand      hand the player is using.
     * @param itemStack item stack being used by the player.
     * @param animation the animation currently being displayed to the player.
     * @return whether the action is allowed to be performed, {@code true} to allow the player to use the item, {@code false} to prevent it.
     */
    public boolean onUseStart(@NotNull LivingEntity entity, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, @NotNull ItemAnimation animation) {
        return true;
    }

    /**
     * Called every tick when the item is being used.
     * <p>
     * On the first time this is called on the next tick after {@link #onUseStart(LivingEntity, PlayerHand, ItemStack, ItemAnimation) onUseStart},
     * if it returned {@code false} this won't be executed.
     *
     * @param entity        entity using the item.
     * @param hand          hand the player is using.
     * @param itemStack     item stack being used by the player.
     * @param usageDuration the time (in ticks) the item has been used for.
     */
    public void onUse(@NotNull LivingEntity entity, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long usageDuration) {
    }

    /**
     * Called once the player has finished or stopped using the item.
     * <p>
     * When canceled, it's possible that the cause is the player disconnecting; in that case the result will be ignored.
     *
     * @param cancelled     whether the item finished or stopped using the item,
     *                      {@code true} if the item has stopped using the item without finishing, {@code false} otherwise.
     * @param player        player that used the item.
     * @param hand          hand the player used.
     * @param itemStack     stack the player used.
     * @param usageDuration the time the player used the item.
     * @return the resulting action to take.
     */
    public UseFinishResult onUseFinished(boolean cancelled, @NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long usageDuration) {
        return UseFinishResult.NONE;
    }

    /**
     * Called when the item blocks an attack.
     * <br><br>
     * This will only be called if {@link #canBlockDamage(Damage, LivingEntity) the item can block the damage}.
     *
     * @param user          the user of the item, and the one that blocked the attack.
     * @param hand          hand the user used.
     * @param stack         stack.
     * @param source        the source damage.
     * @param blockedAmount the amount of damage that was blocked by the item.
     *                      Result of {@link #blockDamage(LivingEntity, PlayerHand, ItemStack, Damage)}.
     */
    public void onBlock(@NotNull LivingEntity user, @NotNull PlayerHand hand, @NotNull ItemStack stack, @NotNull Damage source, float blockedAmount) {
        CardinalEnchantment.bulkHandle(0, stack, ((enchantment, level, value) ->
                enchantment.postDamageBlocked(level, source, stack, user, hand, blockedAmount)));
    }

    /**
     * Whether the item can block damage while it's being used.
     *
     * @param source damage source.
     * @param user the one using the item.
     *
     * @return {@code true} if the item can block damage, {@code false} otherwise.
     */
    public boolean canBlockDamage(@NotNull Damage source, @NotNull LivingEntity user) {
        return false;
    }

    /**
     * Blocks an attack and reduces the damage amount caused by the attack.
     *
     * @param user   the entity using the item.
     * @param hand   the hand the user was using.
     * @param stack  the item stack that's used to block damage.
     * @param source damage source.
     * @return the amount of damage blocked will be subtracted to the total damage.
     */
    public float blockDamage(@NotNull LivingEntity user, @NotNull PlayerHand hand, @NotNull ItemStack stack, @NotNull Damage source) {
        return CardinalEnchantment.bulkHandle(0.0F, stack, (enchantment, level, value) ->
                enchantment.applyBlockingDamageModifier(value, level, source, stack, user, hand));
    }

    /**
     * Disables the shield of the target for a set amount of time.
     * <br><br>
     * Note, the shield item itself may modify the amount of time the shield is disabled for using the resulting value of this.
     * <br>
     * The default shield will apply a scale value to this, if the scale is {@code 0} this won't do anything.
     *
     * @param user   user using this item as a weapon.
     * @param target target of the attack.
     * @param weapon weapon used by the user, aka the stack of this item that the user used.
     * @param shield item stack of the shield.
     * @param source damage source.
     * @return the amount of time (in ticks) the shield will be disabled for.
     */
    public int disableShields(@NotNull LivingEntity user, @NotNull LivingEntity target, @NotNull ItemStack weapon,
                              @NotNull ItemStack shield, @NotNull Damage source) {
        return 0;
    }

    @Override
    public final <T> @Nullable T get(@NotNull DataComponent<@NotNull T> component) {
        return this.prototype.get(component);
    }

    @Override
    public final <T> @Nullable T get(@NotNull RegistryHolder<ServerDataComponent<T>> component) {
        Preconditions.checkNotNull(component, "Registry holder cannot be null!");
        Preconditions.checkArgument(component.referencePresent(), "Holder is empty and does not contain a reference to the component!");

        return component.get().fromTag(component.key(), this.get(DataComponents.CUSTOM_DATA, CustomData.EMPTY));
    }

    /**
     * Defines actions to take after using the item.
     */
    public enum UseFinishResult {

        /**
         * Does nothing once the item has finished being used;
         */
        NONE,

        /**
         * Makes the player use the Riptide Spin Attack.
         */
        RIPTIDE_SPIN_ATTACK
    }

    /**
     * Resulting action to take after {@link #onBlockBreak(Player, ItemStack, Block, Point, BlockFace) onBlockBreak} or
     * {@link #onBlockPlace(Player, PlayerHand, ItemStack, Block, Point, BlockFace) onBlockPlace} are executed.
     */
    public interface BlockInteractionResult {

        /**
         * Whether to cancel the block interaction.
         *
         * @return true if the interaction should be cancelled.
         */
        boolean cancelled();

        /**
         * Block to replace the targeted block.
         *
         * @return a block to replace, or {@code null} for {@link Material#AIR air}.
         */
        default @Nullable Block block() {
            return null;
        }

        /**
         * Cancels the interaction.
         * @return the cancellation result.
         */
        static BlockInteractionResult cancel() {
            return () -> true;
        }

        /**
         * Do nothing.
         *
         * @return a result which does nothing.
         */
        static BlockInteractionResult none() {
            return () -> false;
        }

        /**
         * Replace the targeted block with the specified block.
         *
         * @param block replacement block.
         *
         * @return an interaction result which replaces the targeted block.
         */
        static BlockInteractionResult replace(@NotNull Block block) {
            Preconditions.checkNotNull(block);

            return new BlockInteractionResult() {
                @Override
                public boolean cancelled() {
                    return false;
                }

                @Override
                public Block block() {
                    return block;
                }
            };
        }
    }


    /**
     * Defines action to take when interacted with the world.
     */
    public enum InteractionResult {

        /**
         * Ignores and lets the interaction happen.
         */
        IGNORE,

        /**
         * Blocks the item behavior but allows the block interaction to happen.
         */
        BLOCK,

        /**
         * Cancels the interaction all together, even preventing the interaction to reach the block.
         */
        CANCEL
    }
}
