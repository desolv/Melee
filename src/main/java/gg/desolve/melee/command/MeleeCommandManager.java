package gg.desolve.melee.command;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.Locales;
import co.aikar.commands.PaperCommandManager;
import com.google.common.collect.ImmutableList;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.player.profile.Profile;
import gg.desolve.melee.rank.Rank;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Optional;

public class MeleeCommandManager extends PaperCommandManager {

    public MeleeCommandManager(Plugin plugin) {
        super(plugin);

        try {
            loadCommands();
            loadContexts();
            loadCompletions();

            this.enableUnstableAPI("help");
            this.getLocales().loadYamlLanguageFile("messages.yml", Locales.ENGLISH);
        } catch (Exception ex) {
            Melee.getInstance().getLogger().warning("There was a problem loading command manager.");
            ex.printStackTrace();
        }
    }

    private void loadCommands() {
        Arrays.asList(
                new MeleeCommand()
        ).forEach(this::registerCommand);
    }

    private void loadContexts() {
        getCommandContexts().registerContext(
                Profile.class, c -> {
                    String popName = c.popFirstArg();
                    Profile profile = Profile.getProfile(popName);
                    return Optional.ofNullable(profile).orElseThrow(() ->
                            new InvalidCommandArgument("&cPlayer matching &e" + popName + " &ccould not be found.", false));
                });

        getCommandContexts().registerContext(
                Duration.class, c -> {
                    String popDuration = c.popFirstArg();
                    Duration duration = Duration.fromString(popDuration);
                    return Optional.ofNullable(duration).orElseThrow(() ->
                            new InvalidCommandArgument("&cDuration matching &e" + popDuration + " &ccould not be found.", false));
                });
    }

    private void loadCompletions() {
        getCommandCompletions().registerAsyncCompletion(
                "ranks", c -> Rank.getRanks().keySet()
        );

        getCommandCompletions().registerAsyncCompletion("durations", c ->
                ImmutableList.of("permanent", "s", "m", "h", "d", "w", "M", "y"));

        getCommandCompletions().registerAsyncCompletion("reasons", c ->
                ImmutableList.of("Store", "Whitelist", "Won", "Promoted", "Demoted", "Famous", "Other"));
    }

    @Override
    public boolean hasPermission(CommandIssuer issuer, String permission) {
        CommandSender sender = issuer.getIssuer();

        return sender instanceof ConsoleCommandSender ||
                sender instanceof Player && (issuer.hasPermission("melee.*") ||
                        Arrays.stream(permission.split("\\|")).anyMatch(issuer::hasPermission));
    }

}
