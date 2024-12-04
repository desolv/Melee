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
                "<newline><aqua>Melee Instance Manager"
                        + MeleeServerManager.getServers().stream()
                        .map(server ->
                                "<click:run_command:/instance>" +
                                "<hover:show_text:'<yellow>Hosting a total of online% players"
                                        .replace("online%", String.valueOf(server.getOnline())) +
                                "<newline><newline><green>Click to update instances'>" +
                                "<newline><aqua>@server% vmelee% version% <dark_gray>#id%"
                                        .replace("server%", server.getName())
                                        .replace("melee%", server.getMelee())
                                        .replace("version%", server.getVersion())
                                        .replace("id%", server.getId()) +
                                "<newline><white>Instance has been up for duration% (heartbeat% ago)"
                                        .replace("duration%", Converter.millisToTime(System.currentTimeMillis() - server.getBooting()))
                                        .replace("heartbeat%", Converter.millisToTime(System.currentTimeMillis() - server.getHeartbeat())))
                        .collect(Collectors.joining("<newline>"))
        );
    }

}
