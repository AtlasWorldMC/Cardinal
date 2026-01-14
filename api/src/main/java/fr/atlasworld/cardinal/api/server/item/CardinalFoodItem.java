package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.minestom.server.component.DataComponentMap;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemAnimation;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Consumable;
import net.minestom.server.item.component.Food;
import org.jetbrains.annotations.NotNull;

/**
 * Food item representation
 */
public class CardinalFoodItem extends CardinalItem {
    public CardinalFoodItem(@NotNull Material base, @NotNull DataComponentMap prototype, boolean custom) {
        super(base, prototype, custom);

        Preconditions.checkArgument(prototype.has(DataComponents.FOOD), "Missing '%s' component in item prototype.", DataComponents.FOOD.key());
    }

    /**
     * Retrieve the nutrition value of the food.
     *
     * @return nutrition value of the food.
     */
    public final int nutrition() {
        Food food = this.get(DataComponents.FOOD);
        return food == null ? 0 : food.nutrition();
    }

    /**
     * Retrieve the saturation value of the food.
     *
     * @return saturation value of the food.
     */
    public final float saturation() {
        Food food = this.get(DataComponents.FOOD);
        return food == null ? 0F : food.saturationModifier();
    }

    /**
     * Whether the food can always be eaten.
     *
     * @return {@code true} if the food can always be eaten, {@code false} otherwise.
     */
    public final boolean canAlwaysEat() {
        Food food = this.get(DataComponents.FOOD);
        return food != null && food.canAlwaysEat();
    }

    /**
     * Used to check whether the player can eat the food.
     *
     * @param player player that's going to eat the food.
     *
     * @return {@code true} if the food can be eaten, {@code false} otherwise.
     */
    public boolean canEat(CardinalPlayer player) {
        return this.canAlwaysEat() || player.isHungry();
    }

    @Override
    public boolean onUseStart(@NotNull LivingEntity entity, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, @NotNull ItemAnimation animation) {
        if (!(entity instanceof CardinalPlayer player))
            return true;

        // if the player isn't hungry and this food doesn't allow eating, prevent the action.
        return this.canEat(player);
    }

    @Override
    public UseFinishResult onUseFinished(boolean cancelled, @NotNull Player player, @NotNull PlayerHand hand, @NotNull ItemStack itemStack, long usageDuration) {
        if (cancelled)
            return UseFinishResult.NONE;

        CardinalPlayer cardinalPlayer = (CardinalPlayer) player;
        cardinalPlayer.setFoodSaturation(Math.min(cardinalPlayer.getFoodSaturation() + this.saturation(), 20F));
        cardinalPlayer.setFood(Math.min(cardinalPlayer.getFood() + this.nutrition(), 20));

        if (player.getGameMode() != GameMode.CREATIVE)
            player.setItemInHand(hand, itemStack.consume(1));

        return UseFinishResult.NONE;
    }

    @Override
    public long usingDurationTime() {
        Consumable consumable = this.get(DataComponents.CONSUMABLE);
        if (consumable == null)
            return super.usingDurationTime();

        return consumable.consumeTicks();
    }
}
