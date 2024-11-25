package gg.desolve.melee.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.storage.redis.MeleeSubscriberManager;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

@CommandAlias("melee")
public class MeleeCommand extends BaseCommand {

    @Default
    @CommandPermission("melee.command.melee")
    @Description("Melee's information")
    public static void execute(CommandSender sender) {
        Message.send(sender,
                "<newline><aqua>Melee Management " + Melee.getInstance().getDescription().getVersion()
                        + "<newline><white>Running: <aqua>" + Melee.getInstance().getServer().getVersion()
                        + "<newline><white>Version: <aqua>" + Melee.getInstance().getServer().getBukkitVersion()
                        + "<newline><white>Servers (" + MeleeServerManager.getServers().size() + "): <aqua>" + MeleeServerManager.getServers().stream().map(server -> server.getName() + " v" + server.getMelee()).collect(Collectors.joining("<white>, <aqua>"))
                        + "<newline><white>Listeners (" + MeleeListenerManager.getListeners().size() + "): <aqua>" + String.join("<white>, <aqua>", MeleeListenerManager.getListeners())
                        + "<newline><white>Commands (" + MeleeCommandManager.getCommands().size() + "): <aqua>" + String.join("<white>, <aqua>", MeleeCommandManager.getCommands())
                        + "<newline><white>Subscribers (" + MeleeSubscriberManager.getSubscribers().size() + "): <aqua>" + String.join("<white>, <aqua>", MeleeSubscriberManager.getSubscribers())
        );
    }

}
