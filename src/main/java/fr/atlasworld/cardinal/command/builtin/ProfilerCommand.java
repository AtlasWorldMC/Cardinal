package fr.atlasworld.cardinal.command.builtin;

import fr.atlasworld.cardinal.api.command.condition.PermissionCondition;
import fr.atlasworld.cardinal.bootstrap.Main;
import fr.atlasworld.cardinal.profiler.SparkCommandSenderAdapter;
import fr.atlasworld.cardinal.util.ReflectionUtils;
import me.lucko.spark.common.SparkPlatform;
import me.lucko.spark.common.command.CommandResponseHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.utils.MathUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public final class ProfilerCommand extends Command {
    private static final Component SPARK_PREFIX;
    private static final Component SPACING = Component.text("     ");

    static {
        Component prefix;
        try {
            prefix = (Component) ReflectionUtils.retrieveStaticFieldValue(CommandResponseHandler.class, "PREFIX");
        } catch (ReflectiveOperationException ex) {
            Main.crash("Failed to retrieve spark prefix");
            prefix = null;
        }

        SPARK_PREFIX = prefix;
    }

    private final SparkPlatform platform;
    private final Set<UUID> monitorOpen;

    private final AtomicReference<TickMonitor> lastTick;

    public ProfilerCommand(SparkPlatform platform) {
        super("profiler");
        this.platform = platform;
        this.monitorOpen = new HashSet<>();
        this.lastTick = new AtomicReference<>();

        // Spark
        ArgumentLiteral spark = new ArgumentLiteral("spark");
        ArgumentStringArray sparkArgs = ArgumentType.StringArray("args");
        sparkArgs.setSuggestionCallback(new SparkSuggestion(platform));

        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.profiler.spark"), this::executeSpark, spark, sparkArgs);

        // TPS - Shortcut
        ArgumentLiteral tps = new ArgumentLiteral("tps");
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.profiler.spark"), (sender, context) -> {
            this.platform.executeCommand(new SparkCommandSenderAdapter(sender), new String[]{"tps"});
        }, tps);

        // Monitor
        this.initializeMonitor();
        ArgumentLiteral monitor = new ArgumentLiteral("monitor");
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.profiler.monitor"), this::executeToggleMonitor, monitor);

        this.setCondition(PermissionCondition.of("command.cardinal.profiler"));
    }

    private void executeToggleMonitor(CommandSender sender, CommandContext context) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("You need to be a player to execute this command!");
            return;
        }

        boolean monitorActive = this.monitorOpen.contains(player.getUuid());
        if (monitorActive) {
            this.monitorOpen.remove(player.getUuid());
            sender.sendMessage(SPARK_PREFIX.append(Component.text("Disabled monitor.", NamedTextColor.GRAY)));
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty()); // Reset the tab list
        } else {
            this.monitorOpen.add(player.getUuid());
            sender.sendMessage(SPARK_PREFIX.append(Component.text("Enabled monitor.", NamedTextColor.GRAY)));
        }
    }

    private void initializeMonitor() {
        MinecraftServer.getGlobalEventHandler().addListener(ServerTickMonitorEvent.class, event -> {
            if (this.monitorOpen.isEmpty())
                return;

            this.lastTick.set(event.getTickMonitor());
        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (this.monitorOpen.isEmpty())
                return;

            if (MinecraftServer.getConnectionManager().getOnlinePlayers().isEmpty())
                return;

            long ramUsage = MinecraftServer.getBenchmarkManager().getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            TickMonitor tickMonitor = this.lastTick.get();

            Component footer = Component.newline().append(SPACING)
                    .append(Component.text("Memory Usage: ", NamedTextColor.GRAY)
                            .append(Component.text(ramUsage, NamedTextColor.WHITE)).append(Component.text(" MB", NamedTextColor.GRAY)))
                    .append(SPACING)
                    .append(Component.text("ms/tick: ", NamedTextColor.GRAY).append(Component.text(MathUtils.round(tickMonitor.getTickTime(), 2), computeMsPtColor(tickMonitor.getTickTime()))))
                    .append(Component.text("ms", NamedTextColor.GRAY)).append(SPACING)
                    .appendNewline();

            Audiences.players(player -> this.monitorOpen.contains(player.getUuid()))
                    .sendPlayerListHeaderAndFooter(Component.newline(), footer);
        }, TaskSchedule.nextTick(), TaskSchedule.tick(10));
    }

    private void executeSpark(CommandSender sender, CommandContext context) {
        String[] args = processSparkArgs(context, false);
        if (args == null)
            return;

        this.platform.executeCommand(new SparkCommandSenderAdapter(sender), args);
    }

    private static final class SparkSuggestion implements SuggestionCallback {
        private final SparkPlatform platform;

        public SparkSuggestion(SparkPlatform platform) {
            this.platform = platform;
        }

        @Override
        public void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion) {
            String[] args = processSparkArgs(context, true);
            if (args == null)
                return;

            Iterable<String> suggestionEntries = this.platform.tabCompleteCommand(new SparkCommandSenderAdapter(sender), args);
            for (String suggestionEntry : suggestionEntries) {
                suggestion.addEntry(new SuggestionEntry(suggestionEntry));
            }
        }
    }

    private static String[] processSparkArgs(CommandContext context, boolean tabComplete) {
        String[] split = context.getInput().split(" ", tabComplete ? -1 : 0);
        if (split.length == 0)
            return null;

        return Arrays.copyOfRange(split, 2, split.length);
    }

    private TextColor computeMsPtColor(double tickTime) {
        if (tickTime < 40.0)
            return NamedTextColor.GREEN;

        if (tickTime < 50.0)
            return NamedTextColor.YELLOW;

        return NamedTextColor.RED;
    }
}
