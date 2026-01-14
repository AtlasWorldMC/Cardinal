package fr.atlasworld.cardinal.profiler.tick;

import me.lucko.spark.common.tick.AbstractTickReporter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.server.ServerTickMonitorEvent;

public final class CardinalTickReporter extends AbstractTickReporter {
    private final EventListener<ServerTickMonitorEvent> listener;

    public CardinalTickReporter() {
        this.listener = EventListener.of(ServerTickMonitorEvent.class, event -> this.onTick(event.getTickMonitor().getTickTime()));
    }

    @Override
    public void start() {
        MinecraftServer.getGlobalEventHandler().addListener(this.listener);

    }

    @Override
    public void close() {
        MinecraftServer.getGlobalEventHandler().removeListener(this.listener);
    }
}
