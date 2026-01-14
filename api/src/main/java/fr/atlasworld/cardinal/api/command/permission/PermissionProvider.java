package fr.atlasworld.cardinal.api.command.permission;

import fr.atlasworld.cardinal.api.CardinalServer;
import net.minestom.server.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Permission provider, handle the permission checking of a {@link net.minestom.server.command.CommandSender}.
 */
@FunctionalInterface
public interface PermissionProvider {

    /**
     * Static global helper method to check a sender's permissions.
     *
     * @param sender     sender to check for the permission.
     * @param permission permission to check for.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    static boolean checkPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return CardinalServer.getServer().commandManager().checkPermission(sender, permission).allow();
    }

    /**
     * Check whether the sender has the permission.
     *
     * @param sender     sender to check for the permission.
     * @param permission permission to check for.
     * @return permission result.
     */
    @NotNull PermissionProvider.TriState hasPermission(@NotNull CommandSender sender, @NotNull String permission);

    /**
     * Whether the provider should be replaceable.
     * <p>
     * For example {@code UnsecurePermissionProvider} is marked as replaceable as it acts like a 'fake/dummy' permission provider in-case none is provided.
     *
     * @return {@code true} if the permission provider is replaceable, {@code false} otherwise.
     */
    default boolean replaceable() {
        return false;
    }

    /**
     * Tristate resulting from a permission check.
     * <p>
     * Based of <a href="https://papermc.io/software/velocity">Velocity</a>'s permission system.
     */
    enum TriState {
        ALLOWED(true),
        DENIED(false),
        UNDEFINED(true);

        private final boolean allow;

        TriState(boolean allow) {
            this.allow = allow;
        }

        public boolean allow() {
            return allow;
        }
    }
}
