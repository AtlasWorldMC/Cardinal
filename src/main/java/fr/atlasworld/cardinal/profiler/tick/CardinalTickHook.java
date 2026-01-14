package fr.atlasworld.cardinal.profiler.tick;

import me.lucko.spark.common.tick.AbstractTickHook;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;

public final class CardinalTickHook extends AbstractTickHook {
    private Task hookTask;

    @Override
    public void start() {
        this.hookTask = MinecraftServer.getSchedulerManager()
                .scheduleTask(this::onTick, TaskSchedule.nextTick(), TaskSchedule.nextTick());
    }

    @Override
    public void close() {
        if (this.hookTask == null)
            return;

        this.hookTask.cancel();
        this.hookTask = null;
    }
}
