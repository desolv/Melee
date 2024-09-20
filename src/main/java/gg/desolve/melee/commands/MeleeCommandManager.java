package gg.desolve.melee.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.PaperCommandManager;
import gg.desolve.melee.Melee;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class MeleeCommandManager extends PaperCommandManager {

    public MeleeCommandManager(Plugin plugin) {
        super(plugin);

        try {
            loadCommands();

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

    @Override
    public boolean hasPermission(CommandIssuer issuer, String permission) {
        CommandSender sender = issuer.getIssuer();

        return sender instanceof ConsoleCommandSender ||
                sender instanceof Player && (issuer.hasPermission("melee.*") ||
                        Arrays.stream(permission.split("\\|")).anyMatch(issuer::hasPermission));
    }

}
