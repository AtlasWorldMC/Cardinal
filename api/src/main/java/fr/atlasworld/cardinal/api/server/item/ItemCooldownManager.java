package fr.atlasworld.cardinal.api.server.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.registry.RegistryKey;
import fr.atlasworld.cardinal.api.util.MathUtils;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.UseCooldown;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Item cooldown manager handles the cooldowns of items and the associated packets to send to players.
 */
public class ItemCooldownManager {
    private final Entity entity;
    private final Map<Key, Entry> cooldownEntries;

    public ItemCooldownManager(@NotNull Entity entity) {
        this.entity = entity;
        this.cooldownEntries = new ConcurrentHashMap<>();
    }

    /**
     * Checks whether the provided item is currently on cooldown.
     *
     * @param stack stack to check.
     * @return {@code true} if the item is on cooldown, {@code false} otherwise.
     */
    public boolean isCoolingDown(@NotNull ItemStack stack) {
        return this.cooldownProgress(stack) > 0.0F;
    }

    /**
     * Checks whether the provided item or group key is currently on cooldown.
     *
     * @param key key of the group / item to check.
     * @return {@code true} if the item or group is on cooldown, {@code false} otherwise.
     */
    public boolean isCoolingDown(@NotNull Key key) {
        return this.cooldownProgress(key) > 0.0F;
    }

    /**
     * Retrieve the cooldown progress of the provided item. The progress is a value between {@code 0.0} and {@code 1.0}.
     *
     * @param stack stack to check.
     * @return cooldown progress of the item, between {@code 0.0} and {@code 1.0}.
     */
    public float cooldownProgress(@NotNull ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");

        return this.cooldownProgress(this.getGroup(stack));
    }

    /**
     * Retrieve the cooldown progress of the provided item or group key. The progress is a value between {@code 0.0} and {@code 1.0}.
     *
     * @param key key of the group / item to check.
     * @return cooldown progress of the item or group, between {@code 0.0} and {@code 1.0}.
     */
    public float cooldownProgress(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null");

        Entry entry = this.cooldownEntries.get(key);
        if (entry == null)
            return 0.0F;

        long difference = entry.endTick - entry.startTick;
        long progress = entry.endTick - this.entity.getAliveTicks();
        return MathUtils.clamp((float) progress / difference, 0.0F, 1.0F);
    }

    /**
     * Set the cooldown for the provided item.
     * <br><br>
     * Note, this will set the item {@link UseCooldown} group to be on cooldown, if multiple item share the same group,
     * they will all be set on cooldown.
     *
     * @param stack    item stack to set cooldown for.
     * @param duration duration of the cooldown in ticks.
     */
    public void setCooldown(@NotNull ItemStack stack, int duration) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");
        this.setCooldown(this.getGroup(stack), duration);
    }

    /**
     * Set the cooldown for the provided item / group.
     *
     * @param groupIdentifier item / group identifier.
     * @param duration        duration of the cooldown in ticks.
     */
    public void setCooldown(@NotNull Key groupIdentifier, int duration) {
        Preconditions.checkNotNull(groupIdentifier, "Group identifier cannot be null");
        Preconditions.checkArgument(duration > 0, "Duration must be greater than 0");

        long ticks = this.entity.getAliveTicks();
        this.cooldownEntries.put(groupIdentifier, new Entry(ticks, ticks + duration));
        this.onCooldownStart(groupIdentifier, duration);
    }

    /**
     * Clear the cooldown for the provided item.
     * <br><br>
     * Note, this will reset the item {@link UseCooldown} group, if multiple item share the same group,
     * they will all be reset.
     *
     * @param stack stack to clear cooldown for.
     */
    public void clearCooldown(@NotNull ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");
        this.clearCooldown(this.getGroup(stack));
    }

    /**
     * Clears the cooldown for the provided item / group.
     *
     * @param key key of the group / item to clear cooldown for.
     */
    public void clearCooldown(@NotNull Key key) {
        Preconditions.checkNotNull(key, "Key cannot be null");
        this.onCooldownFinish(key);
    }

    /**
     * Retrieve the registry key of the group, which the provided item belongs to.
     *
     * @param stack stack to retrieve the group from.
     * @return registry key of the group, if the item has no {@link UseCooldown} component, the material registry key will be returned.
     */
    public Key getGroup(@NotNull ItemStack stack) {
        Preconditions.checkNotNull(stack, "ItemStack cannot be null");

        UseCooldown useCooldownComponent = stack.get(DataComponents.USE_COOLDOWN);
        Material material = stack.material();

        Key cooldownGroup = null;
        if (useCooldownComponent != null)
            cooldownGroup = RegistryKey.fromString(useCooldownComponent.cooldownGroup())
                    .orElseThrow(() -> new IllegalArgumentException("Cooldown group identifier is invalid: " + useCooldownComponent.cooldownGroup()));

        return cooldownGroup != null ? cooldownGroup : material.key();
    }

    /**
     * Tick method, used to update the cooldowns.
     */
    public void tick() {
        if (this.cooldownEntries.isEmpty())
            return;

        Iterator<Map.Entry<Key, Entry>> iterator = this.cooldownEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Entry> entry = iterator.next();
            if (entry.getValue().endTick <= this.entity.getAliveTicks()) {
                iterator.remove();
                this.onCooldownFinish(entry.getKey());
            }
        }
    }

    /**
     * Internal method called when a cooldown finishes or is clears.
     *
     * @param groupIdentifier identifier of the group / item that the cooldown finished for.
     */
    protected void onCooldownFinish(Key groupIdentifier) {
        if (!(this.entity instanceof Player player))
            return;

        player.sendPacket(new SetCooldownPacket(groupIdentifier.toString(), 0));
    }

    /**
     * Called when a cooldown starts.
     *
     * @param groupIdentifier identifier of the group / item that the cooldown started for.
     * @param cooldownTicks   the number of ticks the cooldown will last for.
     */
    protected void onCooldownStart(Key groupIdentifier, int cooldownTicks) {
        if (!(this.entity instanceof Player player))
            return;

        player.sendPacket(new SetCooldownPacket(groupIdentifier.toString(), cooldownTicks));
    }

    private record Entry(long startTick, long endTick) {
    }
}
