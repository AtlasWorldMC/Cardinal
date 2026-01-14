package fr.atlasworld.cardinal.command;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.command.CommandManager;
import fr.atlasworld.cardinal.api.command.permission.PermissionProvider;
import fr.atlasworld.cardinal.command.builtin.CardinalCommand;
import fr.atlasworld.cardinal.command.builtin.GameCommand;
import fr.atlasworld.cardinal.command.builtin.TestEnvCommand;
import fr.atlasworld.cardinal.command.console.ConsoleThread;
import fr.atlasworld.cardinal.command.permission.UnsecurePermissionProvider;
import fr.atlasworld.cardinal.util.Logging;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class CardinalCommandManager implements CommandManager {
    private static final Logger LOGGER = Logging.logger();
    public static final Component PREFIX = Component.text("[", NamedTextColor.DARK_GRAY)
            .append(Component.text("\uD83E\uDDED", NamedTextColor.GOLD))
            .append(Component.text("] ", NamedTextColor.DARK_GRAY));

    private final ConsoleThread thread;
    private PermissionProvider provider;

    public static void handleUnknownOrIncompleteCommand(CommandSender sender, String command) {
        sender.sendMessage(PREFIX.append(Component.text("Unknown command or incomplete command: ", NamedTextColor.RED).append(Component.text("/" + command, NamedTextColor.GOLD))));
    }

    public CardinalCommandManager() {
        this.provider = new UnsecurePermissionProvider();
        this.thread = new ConsoleThread();
    }

    public void initialize() {
        MinecraftServer.getCommandManager().setUnknownCommandCallback(CardinalCommandManager::handleUnknownOrIncompleteCommand);
    }

    public void load(boolean reload) {
        if (reload)
            return;

        // Register builtin commands
        MinecraftServer.getCommandManager().register(new CardinalCommand());
        MinecraftServer.getCommandManager().register(new GameCommand());
        MinecraftServer.getCommandManager().register(new TestEnvCommand());
    }

    public void start() {
        this.thread.start();
    }

    public void shutdown(boolean interrupt) {
        this.thread.shutdown();
    }

    @Override
    public void registerCommand(@NotNull Command command) {
        MinecraftServer.getCommandManager().register(command);
    }

    @Override
    public PermissionProvider.TriState checkPermission(@NotNull CommandSender sender, @NotNull String permission) {
        Preconditions.checkNotNull(sender, "Sender cannot be null!");
        Preconditions.checkNotNull(permission, "Permission cannot be null!");

        try {
            PermissionProvider.TriState result = this.provider.hasPermission(sender, permission);
            assert result != null; // Check anyway plugins may not always provide proper results even with annotations.
            return result;
        } catch (Throwable ex) {
            LOGGER.error("Failed to check permission '{}', permission provider failed to provide result:", permission, ex);
            return PermissionProvider.TriState.UNDEFINED;
        }
    }

    @Override
    public void setPermissionProvider(@NotNull PermissionProvider provider) {
        Preconditions.checkNotNull(provider, "Permission provider cannot be null!");

        if (!this.provider.replaceable())
            LOGGER.warn("PermissionProvider conflict, multiple non-replaceable providers registered, replacing '{}' with '{}'. This may be the cause of multiple incompatible plugins.", this.provider.getClass().getName(), provider.getClass().getName());

        this.provider = provider;
    }
}
