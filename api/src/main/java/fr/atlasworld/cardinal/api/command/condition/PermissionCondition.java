package fr.atlasworld.cardinal.api.command.condition;

import com.google.common.base.Preconditions;
import fr.atlasworld.cardinal.api.command.permission.PermissionProvider;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PermissionCondition implements CommandCondition {
    private final @NotNull String permission;

    private PermissionCondition(@NotNull String permission) {
        Preconditions.checkNotNull(permission, "Permission cannot be null!");
        this.permission = permission;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        return PermissionProvider.checkPermission(sender, this.permission);
    }

    public static @NotNull PermissionCondition of(@NotNull String permission) {
        return new PermissionCondition(permission);
    }
}
