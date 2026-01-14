package fr.atlasworld.cardinal.api.command;

import fr.atlasworld.cardinal.api.command.permission.PermissionProvider;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;

public interface CommandManager {

    /**
     * Register a command.
     *
     * @param command command to be registered.
     */
    void registerCommand(@NotNull Command command);

    /**
     * Check the permission of the specified sender.
     *
     * @param sender     sender to check permissions for.
     * @param permission permission to check for.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    PermissionProvider.TriState checkPermission(@NotNull CommandSender sender, @NotNull String permission);

    /**
     * Set the permission provider the server will use to verify permissions.
     *
     * @param provider permission provider.
     */
    void setPermissionProvider(@NotNull PermissionProvider provider);
}
