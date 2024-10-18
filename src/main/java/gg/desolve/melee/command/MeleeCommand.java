package gg.desolve.melee.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.listener.MeleeListenerManager;
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
                        + "\n&fListeners (" + MeleeListenerManager.getListeners().size() + "): &b" + String.join("&f, &b", MeleeListenerManager.getListeners())
                        + "\n&fCommands (" + MeleeCommandManager.getCommands().size() + "): &b" + String.join("&f, &b", MeleeCommandManager.getCommands())
        );
    }

}
