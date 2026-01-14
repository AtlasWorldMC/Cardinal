package fr.atlasworld.cardinal.profiler;

import fr.atlasworld.cardinal.api.command.permission.PermissionProvider;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.ServerSender;
import net.minestom.server.entity.Player;

import java.util.UUID;

public final class SparkCommandSenderAdapter implements me.lucko.spark.common.command.sender.CommandSender {
    private final CommandSender sender;

    public SparkCommandSenderAdapter(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return switch (this.sender) {
            case Player player -> player.getUsername();
            case ConsoleSender ignored -> "Console";
            case ServerSender ignored -> "Server";
            default -> "Unknown";
        };
    }

    @Override
    public UUID getUniqueId() {
        return this.sender instanceof Player player ? player.getUuid() : null;
    }

    @Override
    public void sendMessage(Component component) {
        this.sender.sendMessage(component);
    }

    @Override
    public boolean hasPermission(String permission) {
        return PermissionProvider.checkPermission(this.sender, permission);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SparkCommandSenderAdapter && ((SparkCommandSenderAdapter) obj).sender.equals(this.sender);
    }

    @Override
    public int hashCode() {
        return this.sender.hashCode();
    }
}
