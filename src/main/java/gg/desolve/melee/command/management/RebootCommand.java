package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.Rank;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.server.Reboot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

@CommandAlias("reboot")
public class RebootCommand extends BaseCommand {

    @HelpCommand
    @CommandPermission("melee.command.reboot|melee.command.reboot.help")
    @Syntax("[page]")
    @Description("Shows list of commands")
    public static void onHelp(CommandHelp help) {
        help.setPerPage(6);
        help.showHelp();
    }

    @Subcommand("remove")
    @CommandPermission("melee.command.reboot|melee.command.reboot.remove")
    @Description("Postpones reboot by removing it")
    public static void onRemove(CommandSender sender) {
        MeleeServerManager.getReboot().cancel();
        MeleeServerManager.setReboot(new Reboot(null, 0L, 0L));

        Message.send(sender, "<green>You've postponed the reboot time by removing it.");
    }

    @Subcommand("postpone")
    @CommandCompletion("@rebooting")
    @CommandPermission("melee.command.reboot|melee.command.reboot.postpone")
    @Syntax("<duration>")
    @Description("Postpones reboot by changing timing")
    public static void onPostpone(CommandSender sender, Duration duration) {
        if (Converter.millisToHours(duration.getValue() + 1000) > 48) {
            Message.send(sender, "<red>Reboot can only be lower or equal to 48 hours.");
            return;
        }

        MeleeServerManager.getReboot().cancel();
        MeleeServerManager.setReboot(
                new Reboot(
                        sender instanceof Player ? ((Player) sender).getUniqueId() : null,
                        System.currentTimeMillis(),
                        (duration.getValue() + 1000)
                ));
        MeleeServerManager.getReboot().start();

        Message.send(sender, "<green>You've postponed the reboot time by schedule%."
                .replace("schedule%", Converter.millisToTime(
                        (MeleeServerManager.getReboot().getAddedAt() + MeleeServerManager.getReboot().getDelay())
                                - System.currentTimeMillis()))
        );
    }

    @Subcommand("information")
    @CommandPermission("melee.command.information")
    @Description("Information about the reboot")
    public static void onInformation(CommandSender sender) {
        if (MeleeServerManager.getReboot().getDelay() <= 0) {
            Message.send(sender, "<red>Instance is not scheduled to reboot.");
            return;
        }

        Message.send(sender,
                "<hover:show_text:'<green>Added by player%"
                        .replace("player%",
                                MeleeServerManager.getReboot().getAddedBy() == null ?
                                        "Console" :
                                        Hunter.getHunter(MeleeServerManager.getReboot().getAddedBy()).getUsernameColored()) +
                        "<newline><yellow>Added at date%'>"
                                .replace("date%", Converter.millisToDate(MeleeServerManager.getReboot().getAddedAt())) +
                        "<green>Instance scheduled to reboot in schedule%."
                                .replace("schedule%", Converter.millisToTime(
                                        (MeleeServerManager.getReboot().getAddedAt() + MeleeServerManager.getReboot().getDelay())
                                                - System.currentTimeMillis()))
        );
    }

}
