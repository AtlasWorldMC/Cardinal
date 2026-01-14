package fr.atlasworld.cardinal.command.builtin;

import fr.atlasworld.cardinal.api.command.argument.PluginArgument;
import fr.atlasworld.cardinal.api.command.argument.RegistryBasedArgument;
import fr.atlasworld.cardinal.api.command.condition.GameCondition;
import fr.atlasworld.cardinal.api.command.suggestion.PluginSuggestionCallback;
import fr.atlasworld.cardinal.api.command.suggestion.RegistrySuggestionCallback;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.plugin.Plugin;
import fr.atlasworld.cardinal.api.server.CardinalGames;
import fr.atlasworld.cardinal.api.server.enchantment.CardinalEnchantment;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import fr.atlasworld.cardinal.api.server.item.CardinalItem;
import fr.atlasworld.cardinal.registry.CardinalRegistries;
import fr.atlasworld.cardinal.resource.CardinalResourceManager;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;

import java.util.Optional;

import static fr.atlasworld.cardinal.api.game.builtin.TestEnvLogic.PREFIX;

public final class TestEnvCommand extends Command {
    public TestEnvCommand() {
        super("testenv");

        CommandCondition condition = GameCondition.of(CardinalGames.TEST.get());

        // Give
        ArgumentLiteral give = ArgumentType.Literal("give");
        RegistryBasedArgument<CardinalItem> item = new RegistryBasedArgument<>("item", CardinalRegistries.ITEMS);
        item.setSuggestionCallback(new RegistrySuggestionCallback<>(CardinalRegistries.ITEMS));

        ArgumentInteger amount = ArgumentType.Integer("amount");
        amount.setDefaultValue(1);
        amount.between(1, 6400);

        this.addConditionalSyntax(condition, this::executeGive, give, item, amount);

        // Enchant
        ArgumentLiteral enchant = ArgumentType.Literal("enchant");
        RegistryBasedArgument<CardinalEnchantment> enchantment = new RegistryBasedArgument<>("enchantment", CardinalRegistries.ENCHANTMENTS);
        enchantment.setSuggestionCallback(new RegistrySuggestionCallback<>(CardinalRegistries.ENCHANTMENTS));

        ArgumentInteger level = ArgumentType.Integer("level");
        level.setDefaultValue(1);
        level.between(1, 255);

        this.addConditionalSyntax(condition, this::executeEnchant, enchant, enchantment, level);

        // Resource Pack
        ArgumentLiteral pack = ArgumentType.Literal("pack");
        PluginArgument plugin = new PluginArgument("plugin");
        plugin.setSuggestionCallback(PluginSuggestionCallback.create());

        this.addConditionalSyntax(condition, this::executePackChange, pack, plugin);

        // Gamemode
        ArgumentLiteral gamemode = ArgumentType.Literal("gamemode");
        ArgumentLiteral gm = ArgumentType.Literal("gm");

        ArgumentLiteral creative = ArgumentType.Literal("creative");
        ArgumentLiteral survival = ArgumentType.Literal("survival");
        ArgumentLiteral spectator = ArgumentType.Literal("spectator");
        ArgumentLiteral adventure = ArgumentType.Literal("adventure");

        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.CREATIVE), gamemode, creative);
        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.SURVIVAL), gamemode, survival);
        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.SPECTATOR), gamemode, spectator);
        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.ADVENTURE), gamemode, adventure);

        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.CREATIVE), gm, creative);
        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.SURVIVAL), gm, survival);
        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.SPECTATOR), gm, spectator);
        this.addConditionalSyntax(condition, (sender, ctx) -> this.executeGameMode(sender, GameMode.ADVENTURE), gm, adventure);

        this.setCondition(condition);
    }

    private void executeGameMode(CommandSender sender, GameMode mode) {
        if (!this.validatePlayer(sender))
            return;

        Player player = (Player) sender;

        player.setGameMode(mode);
    }

    private void executePackChange(CommandSender sender, CommandContext ctx) {
        if (!this.validatePlayer(sender))
            return;

        Plugin plugin = ctx.get("plugin");
        Player player = (Player) sender;

        sender.sendMessage(PREFIX.append(Component.text("Fetching resource pack from ", NamedTextColor.GRAY)
                        .append(Component.text("", NamedTextColor.WHITE).append(plugin.name())))
                .append(Component.text("...", NamedTextColor.GRAY)));

        CardinalResourceManager.retrievePackInfo(plugin).thenAccept(info -> {
            if (info == null) {
                sender.sendMessage(PREFIX.append(Component.text("Could not retrieve resource pack info from ", NamedTextColor.RED)
                        .append(Component.text("", NamedTextColor.GOLD).append(plugin.name()))
                        .append(Component.text(".", NamedTextColor.RED))));
                return;
            }

            player.sendResourcePacks(ResourcePackRequest.resourcePackRequest()
                    .replace(true)
                    .required(false)
                    .prompt(Component.text("Load the requested resourcepack ?"))
                    .packs(info)
                    .callback(((uuid, status, audience) -> {
                        switch (status) {
                            case SUCCESSFULLY_LOADED ->
                                    player.sendMessage(PREFIX.append(Component.text("Resource pack loaded successfully!", NamedTextColor.GREEN)));
                            case DECLINED ->
                                    player.sendMessage(PREFIX.append(Component.text("Resource pack loading declined by the player.", NamedTextColor.RED)));
                            case FAILED_DOWNLOAD, FAILED_RELOAD ->
                                    player.sendMessage(PREFIX.append(Component.text("An error occurred while trying to load the resource pack: ", NamedTextColor.RED).append(Component.text(status.name().toLowerCase(), NamedTextColor.GOLD))));
                        }
                    }))
                    .build());
        });
    }

    private void executeGive(CommandSender sender, CommandContext context) {
        if (!this.validatePlayer(sender))
            return;

        Player player = (Player) sender;
        CardinalItem item = context.get("item");
        int amount = context.get("amount");

        if (amount < 1 || amount > 6400) {
            sender.sendMessage(PREFIX.append(Component.text("Invalid amount, must be between 1 and 6400", NamedTextColor.RED)));
            return;
        }

        int loops = amount / item.maxStackSize();
        int remaining = amount % item.maxStackSize();

        for (int i = 0; i < loops; i++) {
            ItemStack stack = item.createStack(item.maxStackSize());
            ItemStack rest = player.getInventory().addItemStack(stack, TransactionOption.ALL);
            if (!rest.isAir())
                player.dropItem(rest);
        }

        if (remaining > 0) {
            ItemStack stack = item.createStack(remaining);
            ItemStack rest = player.getInventory().addItemStack(stack, TransactionOption.ALL);
            if (!rest.isAir())
                player.dropItem(rest);
        }

        sender.sendMessage(PREFIX.append(Component.text("Successfully gave ", NamedTextColor.GRAY)
                .append(Component.text(amount, NamedTextColor.WHITE))
                .append(Component.text(" " + CardinalRegistries.ITEMS.retrieveKey(item).get(), NamedTextColor.WHITE))
                .append(Component.text(".", NamedTextColor.GRAY))));
    }

    private void executeEnchant(CommandSender sender, CommandContext context) {
        if (!this.validatePlayer(sender))
            return;

        Player player = (Player) sender;
        CardinalEnchantment enchantment = context.get("enchantment");
        int level = context.get("level");

        if (level < 1 || level > 255) {
            sender.sendMessage(PREFIX.append(Component.text("Invalid level, must be between 1 and 255.", NamedTextColor.RED)));
            return;
        }

        ItemStack stack = player.getItemInMainHand();
        if (stack.isAir()) {
            sender.sendMessage(PREFIX.append(Component.text("You must be holding an item to enchant it.", NamedTextColor.RED)));
            return;
        }

        stack = enchantment.applyEnchantment(stack, level, true);
        player.setItemInMainHand(stack);

        sender.sendMessage(PREFIX.append(Component.text("Successfully enchanted your item.", NamedTextColor.GREEN)));
    }

    private boolean validatePlayer(CommandSender sender) {
        if (!(sender instanceof CardinalPlayer player)) {
            sender.sendMessage(PREFIX.append(Component.text("Only players can execute this command!", NamedTextColor.RED)));
            return false;
        }

        Optional<GameContainer> container = player.gameContainer();
        if (container.isEmpty() || container.get().game() != CardinalGames.TEST.get()) {
            sender.sendMessage(PREFIX.append(Component.text("You are not allowed to execute this command outside of the test environment!", NamedTextColor.RED)));
            return false;
        }

        Instance instance = player.getInstance();
        if (instance == null || !container.get().isInstanceLinked(instance)) {
            sender.sendMessage(PREFIX.append(Component.text("You are not allowed to execute this command outside of the test environment!", NamedTextColor.RED)));
            return false;
        }

        return true;
    }
}
