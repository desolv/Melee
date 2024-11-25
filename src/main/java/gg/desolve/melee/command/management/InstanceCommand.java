package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.server.MeleeServerManager;
import org.bukkit.command.CommandSender;

import java.util.stream.Collectors;

@CommandAlias("instance")
public class InstanceCommand extends BaseCommand {

    @Default
    @CommandPermission("melee.command.instance")
    @Description("Manage server instances")
    public static void execute(CommandSender sender) {
        Message.send(sender,
                "<newline><aqua>Melee Instance Manager "
                        + "<newline>" + MeleeServerManager.getServers().stream()
                        .map(server ->
                                "<newline><aqua>" + server.getName() + " v" + server.getMelee() + " " + server.getVersion() + " <dark_gray>#" + server.getId()
                                + "<newline><white>Instance has been up for "
                                        + Converter.millisToTime(System.currentTimeMillis() - server.getBooting())
                                        + " (" + Converter.millisToTime(System.currentTimeMillis() - server.getHeartbeat()) + ").<newline>")
                        .collect(Collectors.joining("<newline>"))
        );

    }

}
