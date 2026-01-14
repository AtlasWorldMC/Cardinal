package fr.atlasworld.cardinal.command.builtin;

import fr.atlasworld.cardinal.api.CardinalServer;
import fr.atlasworld.cardinal.api.command.argument.GameContainerArgument;
import fr.atlasworld.cardinal.api.command.argument.RegistryBasedArgument;
import fr.atlasworld.cardinal.api.command.condition.PermissionCondition;
import fr.atlasworld.cardinal.api.command.suggestion.GameContainerSuggestionCallback;
import fr.atlasworld.cardinal.api.command.suggestion.RegistrySuggestionCallback;
import fr.atlasworld.cardinal.api.game.Game;
import fr.atlasworld.cardinal.api.game.GameContainer;
import fr.atlasworld.cardinal.api.game.GameMap;
import fr.atlasworld.cardinal.api.game.GameRule;
import fr.atlasworld.cardinal.api.registry.CardinalRegistries;
import fr.atlasworld.cardinal.api.registry.Registry;
import fr.atlasworld.cardinal.api.server.entity.CardinalPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentLiteral;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static fr.atlasworld.cardinal.command.CardinalCommandManager.PREFIX;

// Minestom command system is a satanic being I wish to no longer interact with, but I am forced to work with it.
// Please put me out of my misery.
// - RaftDev
public final class GameCommand extends Command {

    public GameCommand() {
        super("game");

        ArgumentLiteral list = new ArgumentLiteral("list");
        ArgumentLiteral create = new ArgumentLiteral("create");
        ArgumentLiteral join = new ArgumentLiteral("join");
        ArgumentLiteral spectate = new ArgumentLiteral("spectate");

        GameContainerArgument container = new GameContainerArgument("container");
        container.setSuggestionCallback(new GameContainerSuggestionCallback());

        RegistryBasedArgument<Game> gameArg = new RegistryBasedArgument<>("game", CardinalRegistries.GAMES);
        gameArg.setSuggestionCallback(new RegistrySuggestionCallback<>(CardinalRegistries.GAMES));

        RegistryBasedArgument<GameMap> mapArg = new RegistryBasedArgument<>("map", CardinalRegistries.MAPS);
        mapArg.setSuggestionCallback(new RegistrySuggestionCallback<>(CardinalRegistries.MAPS, (key, map, ctx) -> {
            Game game = ctx.get(gameArg);
            if (game == null)
                return false;

            return map.game().get() == game;
        }));

        this.setCondition(PermissionCondition.of("command.cardinal.game"));
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.game.list"), this::executeList, list);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.game.create"), this::executeCreate, create, gameArg, mapArg);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.game.join"), (sender, context) -> this.executeJoin(sender, context, false), join, container);
        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.game.spectate"), (sender, context) -> this.executeJoin(sender, context, true), spectate, container);

        // Manage command
        ArgumentLiteral manage = new ArgumentLiteral("manage");
        ArgumentLiteral interrupt = new ArgumentLiteral("interrupt");

        this.addConditionalSyntax(PermissionCondition.of("command.cardinal.game.manage.interrupt"), this::executeGameInterrupt, manage, container, interrupt);

        // Game Rules
        ArgumentLiteral rules = new ArgumentLiteral("rules");
        ArgumentLiteral get = new ArgumentLiteral("get");
        ArgumentLiteral set = new ArgumentLiteral("set");

        PermissionCondition getRulePermission = PermissionCondition.of("command.cardinal.game.manage.rules.get");
        PermissionCondition setRulePermission = PermissionCondition.of("command.cardinal.game.manage.rules.set");

        for (Map.Entry<Key, GameRule<?>> entry : CardinalRegistries.GAME_RULES.entries()) {
            Argument<?> arg = entry.getValue().argument();
            ArgumentLiteral rule = new ArgumentLiteral(entry.getKey().toString());

            this.addConditionalSyntax(getRulePermission, (sender, context) -> this.executeGetGameRuleValue(sender, context, entry.getValue(), entry.getKey()), manage, container, rules, rule, get);
            this.addConditionalSyntax(setRulePermission, (sender, context) -> this.executeSetGameRuleValue(sender, context, entry.getValue(), entry.getKey()), manage, container, rules, rule, set, arg);
        }

        // See https://github.com/Minestom/Minestom/issues/2682 - Default executors break suggestion providers
        // this.setDefaultExecutor((sender, context) -> CardinalCommandManager.handleUnknownOrIncompleteCommand(sender, context.getInput()));
    }

    private void executeList(CommandSender sender, CommandContext context) {
        Set<GameContainer> containers = CardinalServer.getServer().gameManager().activeGames();

        Component baseMessage = PREFIX.append(Component.text("Currently ", NamedTextColor.GRAY)
                        .append(Component.text(containers.size(), NamedTextColor.WHITE)).append(Component.text(" game(s) are available:", NamedTextColor.GRAY)))
                .appendNewline().append(Component.text(""));

        Iterator<GameContainer> games = containers.iterator();
        while (games.hasNext()) {
            GameContainer container = games.next();
            Component containerComponent = Component.text("", NamedTextColor.WHITE).append(this.createGameComponent(container));

            baseMessage = baseMessage.append(containerComponent);
            if (games.hasNext())
                baseMessage = baseMessage.append(Component.text(", ", NamedTextColor.GRAY));
        }

        sender.sendMessage(baseMessage);
    }

    private void executeCreate(CommandSender sender, CommandContext context) {
        Game game = context.get("game");
        GameMap map = context.get("map");

        if (map.game().get() != game) {
            sender.sendMessage(PREFIX.append(Component.text("The map ", NamedTextColor.RED).append(Component.text("", NamedTextColor.GOLD)
                            .append(map.name())).append(Component.text(" is not compatible with ", NamedTextColor.RED)))
                    .append(Component.text("", NamedTextColor.GOLD).append(game.displayName()))
                    .append(Component.text(".", NamedTextColor.RED)));
            return;
        }

        GameContainer container = CardinalServer.getServer().gameManager().createGame(game, map);
        sender.sendMessage(PREFIX.append(Component.text("Successfully created game '", NamedTextColor.GREEN)
                .append(this.createGameComponent(container).color(NamedTextColor.GREEN)).append(Component.text("'.", NamedTextColor.GREEN))));
    }

    private void executeJoin(CommandSender sender, CommandContext context, boolean spectate) {
        if (!(sender instanceof CardinalPlayer player)) {
            sender.sendMessage(PREFIX.append(Component.text("Only players can execute this command.", NamedTextColor.RED)));
            return;
        }

        GameContainer container = context.get("container");
        Optional<GameContainer> playerContainer = player.gameContainer();
        boolean isPlayerSpectator = player.isGameSpectator();

        if (playerContainer.isPresent() && playerContainer.get() == container && spectate == isPlayerSpectator) {
            sender.sendMessage(PREFIX.append(Component.text("You are already in this game.", NamedTextColor.RED)));
            return;
        }

        sender.sendMessage(PREFIX.append(Component.text("Sending you to ", NamedTextColor.GRAY)
                .append(this.createGameComponent(container).color(NamedTextColor.WHITE))
                .append(Component.text("...", NamedTextColor.GRAY))));

        container.addPlayer(player, spectate).thenAccept(accepted -> {
            if (!accepted) {
                sender.sendMessage(PREFIX.append(Component.text("Could not send you to ", NamedTextColor.RED)
                        .append(this.createGameComponent(container).color(NamedTextColor.GOLD)
                                .append(Component.text(".", NamedTextColor.RED)))));
                return;
            }

            sender.sendMessage(PREFIX.append(Component.text("Successfully joined ", NamedTextColor.GREEN)
                    .append(this.createGameComponent(container).color(NamedTextColor.GREEN)
                            .append(Component.text(".", NamedTextColor.GREEN)))));
        });
    }

    @SuppressWarnings("unchecked")
    private void executeGetGameRuleValue(CommandSender sender, CommandContext context, GameRule<?> rule, Key ruleKey) {
        GameContainer container = context.get("container");
        Object value = container.getRuleValue(rule);

        if (value != null && rule.argument() instanceof RegistryBasedArgument<?> registryArgument) {
            Registry<? super Object> registry = (Registry<? super Object>) registryArgument.getRegistry();
            Optional<Key> key = registry.retrieveKey(value);

            if (key.isPresent()) {
                sender.sendMessage(PREFIX.append(Component.text(ruleKey.toString(), NamedTextColor.WHITE))
                        .append(Component.text(" is set to: ", NamedTextColor.GRAY))
                        .append(Component.text(key.get().toString(), NamedTextColor.WHITE)));

                return;
            }
        }

        sender.sendMessage(PREFIX.append(Component.text(ruleKey.toString(), NamedTextColor.WHITE))
                .append(Component.text(" is set to: ", NamedTextColor.GRAY))
                .append(Component.text(String.valueOf(value), NamedTextColor.WHITE)));
    }

    @SuppressWarnings("unchecked")
    private void executeSetGameRuleValue(CommandSender sender, CommandContext context, GameRule<?> rule, Key ruleKey) {
        GameContainer container = context.get("container");
        Object value = context.get(rule.argument());

        container.setGameRule((GameRule<? super Object>) rule, value);

        if (rule.argument() instanceof RegistryBasedArgument<?> registryArgument) {
            Registry<? super Object> registry = (Registry<? super Object>) registryArgument.getRegistry();
            Optional<Key> key = registry.retrieveKey(value);

            if (key.isPresent()) {
                sender.sendMessage(PREFIX.append(Component.text("Successfully set '", NamedTextColor.GREEN))
                        .append(Component.text(ruleKey.toString(), NamedTextColor.GREEN))
                        .append(Component.text("' to: ", NamedTextColor.GREEN))
                        .append(Component.text(key.get().toString(), NamedTextColor.GREEN)));

                return;
            }
        }

        sender.sendMessage(PREFIX.append(Component.text("Successfully set '", NamedTextColor.GREEN))
                .append(Component.text(ruleKey.toString(), NamedTextColor.GREEN))
                .append(Component.text("' to: ", NamedTextColor.GREEN))
                .append(Component.text(String.valueOf(value), NamedTextColor.GREEN)));
    }

    private void executeGameInterrupt(CommandSender sender, CommandContext context) {
        GameContainer container = context.get("container");
        boolean interrupted = container.interrupt();

        if (interrupted)
            sender.sendMessage(PREFIX.append(Component.text("Successfully interrupted: ", NamedTextColor.GREEN))
                .append(Component.text(container.displayName(), NamedTextColor.GREEN)));
        else
            sender.sendMessage(PREFIX.append(Component.text("Could not cancel: ", NamedTextColor.RED))
                    .append(Component.text(container.displayName(), NamedTextColor.GOLD)));
    }


    private Component createGameComponent(@NotNull GameContainer container) {
        Game game = container.game();

        Component hoverText = Component.text("Game: ", NamedTextColor.GRAY).append(Component.text("", NamedTextColor.WHITE)
                .append(game.displayName())).appendNewline();

        hoverText = hoverText.append(Component.text("Map: ", NamedTextColor.GRAY).append(Component.text("", NamedTextColor.WHITE)
                .append(container.map().name()))).appendNewline();
        hoverText = hoverText.append(Component.text("State: ", NamedTextColor.GRAY).append(Component.text(container.state().name().toLowerCase(), NamedTextColor.WHITE)))
                .appendNewline();
        hoverText = hoverText.append(Component.text("Players: ", NamedTextColor.GRAY).append(Component.text("unsupported", NamedTextColor.WHITE)))
                .appendNewline();

        if (container.state().isJoinable())
            hoverText = hoverText.appendNewline().append(Component.text("Click to join the game.", NamedTextColor.GRAY));

        return Component.text(container.displayName(), container.state().isJoinable() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .hoverEvent(hoverText).clickEvent(container.state().isJoinable() ? ClickEvent.runCommand("game join " + container.displayName()) : null);
    }
}
