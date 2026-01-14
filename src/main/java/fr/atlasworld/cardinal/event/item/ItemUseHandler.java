package fr.atlasworld.cardinal.event.item;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.server.entity.CardinalEntity;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.api.server.item.ItemCooldownManager;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.item.PlayerBeginItemUseEvent;
import net.minestom.server.event.item.PlayerCancelItemUseEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemUseHandler {
    private static final Logger LOGGER = Logging.logger();
    private static final Map<UUID, UseTask> TASKS = new ConcurrentHashMap<>();

    public static void onUseStart(PlayerBeginItemUseEvent event, CardinalItem item) {
        ItemCooldownManager cooldownManager = ((CardinalEntity) event.getPlayer()).cooldownManager();
        if (cooldownManager.isCoolingDown(event.getItemStack())) {
            event.setCancelled(true);
            return;
        }

        boolean allow = item.onUseStart(event.getPlayer(), event.getHand(), event.getItemStack(), event.getAnimation());
        if (!allow) {
            event.setCancelled(true);
            return;
        }

        UseTask.schedule(event, item);
        // event.setItemUseDuration(Math.max(0, item.usingDurationTime())); - FIXME: Always throwing warning about user already using an item.
    }

    public static void onUseFinish(PlayerFinishItemUseEvent event, CardinalItem item) {
        UseTask task = TASKS.get(event.getPlayer().getUuid());
        if (task == null)
            return;

        CardinalItem.UseFinishResult result = task.finish();
        event.setRiptideSpinAttack(result == CardinalItem.UseFinishResult.RIPTIDE_SPIN_ATTACK);
    }

    public static void onUseCancel(PlayerCancelItemUseEvent event, CardinalItem item) {
        UseTask task = TASKS.get(event.getPlayer().getUuid());
        if (task == null)
            return;

        CardinalItem.UseFinishResult result = task.cancel();
        event.setRiptideSpinAttack(result == CardinalItem.UseFinishResult.RIPTIDE_SPIN_ATTACK);
    }

    private static final class UseTask {
        private final CardinalItem item;

        private final Player player;
        private final PlayerHand hand;
        private final ItemStack stack;

        private Task task;
        private long usageTime;

        public synchronized static void schedule(PlayerBeginItemUseEvent event, CardinalItem item) {
            Preconditions.checkNotNull(event, "Event cannot be null");
            Preconditions.checkNotNull(item, "Item cannot be null");

            Player player = event.getPlayer();
            UseTask oldTask = TASKS.get(player.getUuid());

            if (oldTask != null) {
                LOGGER.warn("Player '{}' is trying to use an item but is already using in item.", player.getUsername());
                oldTask.cancel();
            }

            UseTask task = new UseTask(item, event.getPlayer(), event.getHand(), event.getItemStack());
            TASKS.put(player.getUuid(), task);

            Task scheduledTask = MinecraftServer.getSchedulerManager().scheduleTask(task::tick, TaskSchedule.nextTick(), TaskSchedule.nextTick());
            task.updateTaskReference(scheduledTask);
        }

        private UseTask(CardinalItem item, Player player, PlayerHand hand, ItemStack stack) {
            this.item = item;
            this.player = player;
            this.hand = hand;
            this.stack = stack;

            this.usageTime = 0;
        }

        private void updateTaskReference(@NotNull Task task) {
            Preconditions.checkNotNull(task, "Task cannot be null");
            Preconditions.checkState(this.task == null, "Task is already set!");
            this.task = task;
        }

        private void tick() {
            if (!this.player.isOnline()) {
                LOGGER.debug("Player '{}' is no longer online, cancelling usage task.", this.player.getUsername());
                this.cancel(); // Ignore the resulting action as the player disconnected.
                return;
            }

            if (!this.player.isUsingItem()) {
                this.cancel();
                return;
            }

            try {
                this.item.onUse(this.player, this.hand, this.stack, this.usageTime);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass item usage event to {}:", this.item.getClass().getSimpleName(), ex);
            }

            this.usageTime++;
        }

        private CardinalItem.UseFinishResult cancel() {
            this.terminate();

            try {
                return this.item.onUseFinished(true, this.player, this.hand, this.stack, this.usageTime);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass item usage finished event to {}:", this.item.getClass().getSimpleName(), ex);
                return CardinalItem.UseFinishResult.NONE;
            }
        }

        private CardinalItem.UseFinishResult finish() {
            this.terminate();

            try {
                return this.item.onUseFinished(false, this.player, this.hand, this.stack, this.usageTime);
            } catch (Throwable ex) {
                LOGGER.error("Failed to pass item usage finished event to {}:", this.item.getClass().getSimpleName(), ex);
                return CardinalItem.UseFinishResult.NONE;
            }
        }

        private void terminate() {
            this.task.cancel();
            TASKS.remove(this.player.getUuid());
        }
    }
}
