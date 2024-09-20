package gg.desolve.melee.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.melee.Melee;
import gg.desolve.melee.commons.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("melee")
public class MeleeCommand extends BaseCommand {

    @Default
    @CommandPermission("melee.command.melee")
    @Description("Melee's information")
    public static void execute(CommandSender sender) {
        Message.send(sender,
                "\n&bMelee Management " + Melee.getInstance().getDescription().getVersion()
                        + "\n&fRunning: &b" + Melee.getInstance().getServer().getVersion()
                        + "\n&fVersion: &b" + Melee.getInstance().getServer().getBukkitVersion()
                        + "\n&fPlayers: &b" + Bukkit.getOnlinePlayers().size()
        );
    }

}
