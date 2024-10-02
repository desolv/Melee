package gg.desolve.melee.command;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import gg.desolve.melee.Melee;
import gg.desolve.melee.player.profile.Profile;
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

            this.enableUnstableAPI("help");
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
    }

    @Override
    public boolean hasPermission(CommandIssuer issuer, String permission) {
        CommandSender sender = issuer.getIssuer();

        return sender instanceof ConsoleCommandSender ||
                sender instanceof Player && (issuer.hasPermission("melee.*") ||
                        Arrays.stream(permission.split("\\|")).anyMatch(issuer::hasPermission));
    }

}
