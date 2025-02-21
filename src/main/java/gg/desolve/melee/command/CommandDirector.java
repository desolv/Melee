package gg.desolve.melee.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import gg.desolve.melee.Melee;
import gg.desolve.melee.command.management.GrantManualCommand;
import gg.desolve.melee.command.management.InvalidateGrantCommand;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.command.CommandManager;
import gg.desolve.mithril.instance.Instance;
import gg.desolve.mithril.relevance.Duration;
import gg.desolve.mithril.relevance.Message;
import gg.desolve.mithril.relevance.Scope;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class CommandDirector {

    private final CommandManager commandManager;
    private final List<BaseCommand> commands;

    public CommandDirector(CommandManager commandManager) {
        this.commandManager = commandManager;
        contexts();
        completions();
        commands = commands();
    }

    private List<BaseCommand> commands() {
        List<BaseCommand> commandList = Arrays.asList(
                new GrantManualCommand(),
                new InvalidateGrantCommand()
        );

        commandList.forEach(commandManager::registerCommand);
        return commandList;
    }

    private void contexts() {
        commandManager.getCommandContexts().registerContext(
                Profile.class, p -> {
                    String popName = p.popFirstArg();
                    Profile profile = Melee.getInstance().getProfileManager().retrieve(popName);
                    if (profile == null || profile.getUsername() == null)
                        throw new InvalidCommandArgument(Message.translate("<red>Username matching <yellow>" + popName + " <red>not found."), false);
                    return profile;
                });

        commandManager.getCommandContexts().registerContext(
                Rank.class, r -> {
                    String popName = r.popFirstArg();
                    Rank rank = Melee.getInstance().getRankManager().retrieve(popName);

                    if (rank == null || rank.isPrimary())
                        throw new InvalidCommandArgument(Message.translate(
                                rank == null
                                        ? "<red>Rank matching <yellow>" + popName + " <red>not found."
                                        : "<red>Unable to grant the default rank."
                        ), false);
                    return rank;
                });

        commandManager.getCommandContexts().registerContext(
                Scope.class, s -> {
                    String popName = s.popFirstArg();
                    Scope scope = new Scope(popName);

                    List<String> instances = Mithril.getInstance().getInstanceManager().retrieve().stream().map(Instance::getName).toList();
                    List<String> removed = new ArrayList<>();

                    List<String> scopes = new ArrayList<>(scope.getScopes());

                    scopes.removeIf(s2 -> {
                        if (!instances.contains(s2) && !s2.equalsIgnoreCase("global")) {
                            removed.add(s2);
                            return true;
                        }
                        return false;
                    });

                    scope.setScopes(scopes);

                    if (!removed.isEmpty())
                        throw new InvalidCommandArgument(Message.translate("<red>Scope/s matching <yellow>" + String.join(", ", removed) + " <red>not found."), false);
                    return scope;
                }
        );

        commandManager.getCommandContexts().registerContext(
                Instance.class, i -> {
                    String popName = i.popFirstArg();
                    Instance instance = Mithril.getInstance().getInstanceManager().retrieve(popName);
                    return Optional.ofNullable(instance).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Instance matching <yellow>" + popName + " <red>not found."), false));
                });

        commandManager.getCommandContexts().registerContext(
                Duration.class, d -> {
                    String popName = d.popFirstArg();
                    Duration duration = Duration.duration(popName);
                    return Optional.ofNullable(duration).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Duration matching <yellow>" + popName + " <red>not found."), false));
                });
    }


    private void completions() {
        commandManager.getCommandCompletions().registerAsyncCompletion("ranks", s ->
                Melee.getInstance().getRankManager().retrieve().stream().map(Rank::getName).collect(Collectors.toList()));

        commandManager.getCommandCompletions().registerAsyncCompletion("scopes", s ->
                Stream.concat(
                        Stream.of("global"),
                        Mithril.getInstance().getInstanceManager().retrieve().stream().map(Instance::getName)
                ).collect(Collectors.toList()));

        commandManager.getCommandCompletions().registerAsyncCompletion("instances", s ->
                Mithril.getInstance().getInstanceManager().retrieve().stream().map(Instance::getName).toList());

        commandManager.getCommandCompletions().registerAsyncCompletion("durations", s ->
                Arrays.asList(Mithril.getInstance().getLanguageConfig().getString("server.durations").split("\\|")));
    }
}

