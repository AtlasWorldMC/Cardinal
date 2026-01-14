package fr.atlasworld.cardinal.api.command.condition;

import com.google.common.base.Preconditions;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.condition.CommandCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Multi condition, allows to combine multiple conditions.
 */
public final class MultiCondition implements CommandCondition {
    private final CommandCondition[] conditions;

    private MultiCondition(CommandCondition... conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender, @Nullable String commandString) {
        for (CommandCondition condition : this.conditions) {
            if (!condition.canUse(sender, commandString))
                return false;
        }

        return true;
    }

    public static @NotNull MultiCondition of(@NotNull CommandCondition @NotNull ... conditions) {
        Preconditions.checkNotNull(conditions, "Conditions cannot be null");
        return new MultiCondition(conditions);
    }
}
