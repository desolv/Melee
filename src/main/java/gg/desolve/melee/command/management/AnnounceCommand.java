package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.common.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@CommandAlias("announce")
public class AnnounceCommand extends BaseCommand {

    @Default
    @CommandPermission("melee.command.announce")
    @Syntax("<message>")
    @Description("Announce a message to the server")
    public static void execute(CommandSender sender, String message) {
        Bukkit.broadcastMessage(Message.translate(message));
    }

}
