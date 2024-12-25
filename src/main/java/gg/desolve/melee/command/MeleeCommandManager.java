package gg.desolve.melee.command;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.Locales;
import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import gg.desolve.melee.Melee;
import gg.desolve.melee.command.common.PlaytimeCommand;
import gg.desolve.melee.command.management.*;
import gg.desolve.melee.command.moderation.BanCommand;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.common.Reason;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.server.Scope;
import gg.desolve.melee.server.Server;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeleeCommandManager extends PaperCommandManager {

    @Getter
    private static final List<String> commands = new ArrayList<>();

    public MeleeCommandManager(Plugin plugin) {
        super(plugin);

        try {
            loadContexts();
            loadCommands();
            loadCompletions();

            this.enableUnstableAPI("help");
            this.getLocales().loadYamlLanguageFile("acf-lang.yml", Locales.ENGLISH);
        } catch (Exception ex) {
            Melee.getInstance().getLogger().warning("There was a problem loading command manager.");
            ex.printStackTrace();
        }
    }

    private void loadCommands() {
        Arrays.asList(
                new MeleeCommand(),
                new GrantManualCommand(),
                new InvalidateGrantCommand(),
                new RankCommand(),
                new InstanceCommand(),
                new AnnounceCommand(),
                new GrantCommand(),
                new RebootCommand(),
                new PlaytimeCommand(),
                new BanCommand()
        ).forEach(command -> {
            registerCommand(command);
            commands.add(command.getClass().getSimpleName());
        });
    }

    private void loadContexts() {
        getCommandContexts().registerContext(
                Hunter.class, c -> {
                    String popName = c.popFirstArg();
                    Hunter hunter = Hunter.getHunter(popName);
                    return Optional.ofNullable(hunter).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Player matching <yellow>" + popName + " <red>could not be found."), false));
                });

        getCommandContexts().registerContext(
                Duration.class, c -> {
                    String popDuration = c.popFirstArg();
                    Duration duration = Duration.fromString(popDuration);
                    return Optional.ofNullable(duration).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Duration matching <yellow>" + popDuration + " <red>could not be found."), false));
                });

        getCommandContexts().registerContext(
                Rank.class, c -> {
                    String popRank = c.popFirstArg();
                    Rank rank = Rank.getRank(popRank);
                    return Optional.ofNullable(rank).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Rank matching <yellow>" + popRank + " <red>could not be found."), false));
                });

        getCommandContexts().registerContext(
                Scope.class, c -> {
                    String popScope = c.popFirstArg();
                    Scope scope = Scope.fromString(popScope);
                    return Optional.ofNullable(scope).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Scope matching <yellow>" + popScope + " <red>could not be found."), false));
                });

        getCommandContexts().registerContext(
                Reason.class, c -> {
                    String popReason = c.popFirstArg();
                    Reason reason = Reason.fromString(popReason);
                    return Optional.of(reason).orElseThrow(() ->
                            new InvalidCommandArgument(Message.translate("<red>Reason matching <yellow>" + popReason + " <red>could not be found."), false));
                });
    }

    private void loadCompletions() {
        getCommandCompletions().registerAsyncCompletion(
                "ranks", c -> Rank.getRanks().keySet()
        );

        getCommandCompletions().registerAsyncCompletion("durations", d ->
                ImmutableList.of("permanent", "s", "m", "h", "d", "w", "M", "y"));

        getCommandCompletions().registerAsyncCompletion("rebooting", d ->
                ImmutableList.of("now", "s", "m", "h", "d", "w", "M", "y"));

        getCommandCompletions().registerAsyncCompletion("reasons", r ->
                ImmutableList.of("Store", "Whitelist", "Won", "Promoted", "Demoted", "Famous", "Other"));

        getCommandCompletions().registerAsyncCompletion("scopes", s ->
                Stream.concat(
                        Stream.of("global"),
                        MeleeServerManager.getServers()
                                .stream()
                                .map(Server::getName)
                ).collect(Collectors.toList()));

        getCommandCompletions().registerAsyncCompletion("type", r ->
                ImmutableList.of("-s", "-silent", "-p", "-public"));
    }

    @Override
    public boolean hasPermission(CommandIssuer issuer, String permission) {
        CommandSender sender = issuer.getIssuer();

        return sender instanceof ConsoleCommandSender ||
                sender instanceof Player && (issuer.hasPermission("melee.*") ||
                        Arrays.stream(permission.split("\\|")).anyMatch(issuer::hasPermission));
    }

}
