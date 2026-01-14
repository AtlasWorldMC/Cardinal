package fr.atlasworld.cardinal.command.builtin;

import com.google.common.base.Stopwatch;
import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.command.condition.PermissionCondition;
import fr.atlasworld.cardinal.api.command.permission.PermissionProvider;
import fr.atlasworld.cardinal.api.data.Datapack;
import fr.atlasworld.cardinal.api.data.Meta;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.bootstrap.BuildInfo;
import fr.atlasworld.cardinal.bootstrap.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;

import java.util.Iterator;
import java.util.Set;

import static fr.atlasworld.cardinal.command.CardinalCommandManager.PREFIX;

public final class CardinalCommand extends Command {
    public CardinalCommand() {
        super("cardinal");

        ArgumentLiteral version = new ArgumentLiteral("version");
        ArgumentLiteral datapacks = new ArgumentLiteral("datapacks");
        ArgumentLiteral plugins = new ArgumentLiteral("plugins");
        ArgumentLiteral reload = new ArgumentLiteral("reload");
        ArgumentLiteral stop = new ArgumentLiteral("stop");

        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.cardinal.version"), this::executeVersion, version);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.cardinal.datapacks"), this::executeDatapacks, datapacks);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.cardinal.plugins"), this::executePlugins, plugins);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.cardinal.reload"), this::executeReload, reload);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.cardinal.stop"), this::executeStop, stop);

        this.setCondition(((sender, commandString) -> PermissionProvider.checkPermission(sender, "command.cardinal.cardinal")));

        // See https://github.com/Minestom/Minestom/issues/2682 - Default executors break suggestion providers
        // this.setDefaultExecutor((sender, context) -> CardinalCommandManager.handleUnknownOrIncompleteCommand(sender, context.getInput()));
    }

    private void executeVersion(CommandSender sender, CommandContext context) {
        sender.sendMessage(PREFIX.append(Component.text("Currently running", NamedTextColor.GRAY))
                .append(Component.text(" Cardinal " + BuildInfo.version(), NamedTextColor.WHITE))
                .append(Component.text(" for ", NamedTextColor.GRAY))
                .append(Component.text("Minecraft " + MinecraftServer.VERSION_NAME, NamedTextColor.WHITE)));
    }

    private void executeDatapacks(CommandSender sender, CommandContext context) {
        Set<Datapack> dataPacks = CardinalServer.getServer().dataManager().loadedDatapacks();

        Component baseMessage = PREFIX.append(Component.text("Currently ", NamedTextColor.GRAY)
                .append(Component.text(dataPacks.size(), NamedTextColor.WHITE)).append(Component.text(" datapack(s) are loaded:", NamedTextColor.GRAY))).appendNewline();

        Iterator<Datapack> packs = dataPacks.iterator();
        while (packs.hasNext()) {
            Datapack pack = packs.next();
            Component packComponent = Component.text("", NamedTextColor.WHITE).append(this.createMetaComponent(pack));

            baseMessage = baseMessage.append(packComponent);
            if (packs.hasNext())
                baseMessage = baseMessage.append(Component.text(", ", NamedTextColor.GRAY));
        }

        sender.sendMessage(baseMessage);
    }

    private void executePlugins(CommandSender sender, CommandContext context) {
        Set<Plugin> plugins = CardinalServer.getServer().pluginManager().loadedPlugins();

        Component baseMessage = PREFIX.append(Component.text("Currently ", NamedTextColor.GRAY)
                .append(Component.text(plugins.size(), NamedTextColor.WHITE)).append(Component.text(" plugin(s) are loaded:", NamedTextColor.GRAY))).appendNewline();

        Iterator<Plugin> pluginIterator = plugins.iterator();
        while (pluginIterator.hasNext()) {
            Plugin plugin = pluginIterator.next();
            Component packComponent = Component.text("", NamedTextColor.WHITE).append(this.createMetaComponent(plugin));

            baseMessage = baseMessage.append(packComponent);
            if (pluginIterator.hasNext())
                baseMessage = baseMessage.append(Component.text(", ", NamedTextColor.GRAY));
        }

        sender.sendMessage(baseMessage);
    }

    private void executeReload(CommandSender sender, CommandContext context) {
        sender.sendMessage(PREFIX.append(Component.text("Reloading server...", NamedTextColor.GRAY)));

        Stopwatch reloadWatch = Stopwatch.createStarted();
        ((fr.atlasworld.cardinal.CardinalServer) CardinalServer.getServer()).load(true);
        reloadWatch.stop();

        sender.sendMessage(PREFIX.append(Component.text("Server reloaded in ", NamedTextColor.GRAY))
                .append(Component.text(reloadWatch.elapsed().toMillis(), NamedTextColor.WHITE))
                .append(Component.text(" ms.", NamedTextColor.GRAY)));
    }

    private void executeStop(CommandSender sender, CommandContext context) {
        sender.sendMessage(PREFIX.append(Component.text("Stopping server...", NamedTextColor.RED)));

        // Current patch, for https://github.com/Minestom/Minestom/issues/2907
        MinecraftServer.getSchedulerManager().scheduleEndOfTick(Main::shutdown);
    }

    private Component createMetaComponent(Meta meta) {
        Component authorsComponent = Component.empty();
        Iterator<Component> authors = meta.authors().iterator();
        while (authors.hasNext()) {
            Component author = authors.next();
            authorsComponent = authorsComponent.append(author);
            if (authors.hasNext())
                authorsComponent = authorsComponent.append(Component.text(", ", NamedTextColor.GRAY));
        }

        Component hoverText = Component.text("Version: ", NamedTextColor.GRAY).append(Component.text(meta.version(), NamedTextColor.WHITE)).appendNewline()
                .append(Component.text("Authors: ", NamedTextColor.GRAY).append(Component.text("", NamedTextColor.WHITE).append(authorsComponent))).appendNewline().appendNewline()
                .append(meta.description());

        return meta.name().hoverEvent(hoverText);
    }
}
