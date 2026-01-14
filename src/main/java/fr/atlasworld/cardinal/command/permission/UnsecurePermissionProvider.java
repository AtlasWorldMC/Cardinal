package fr.atlasworld.cardinal.command.permission;

import fr.atlasworld.cardinal.api.command.permission.PermissionProvider;
import fr.atlasworld.cardinal.util.Logging;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class UnsecurePermissionProvider implements PermissionProvider {
    private static final Logger LOGGER = Logging.logger();
    private static boolean warned = false;

    @Override
    public @NotNull PermissionProvider.TriState hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (!warned) {
            LOGGER.warn("Using unsecure permission provider, all permission will be granted without checking. This is not recommended for production environment.");
            warned = true;
        }

        return TriState.ALLOWED;
    }
}
