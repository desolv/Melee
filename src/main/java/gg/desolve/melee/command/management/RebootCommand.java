package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Duration;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.server.Reboot;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    @Subcommand("invalidate")
    @CommandPermission("melee.command.reboot|melee.command.reboot.invalidate")
    @Description("Postpones reboot by removing it")
    public static void onRemove(CommandSender sender) {
        if (MeleeServerManager.getReboot().getDelay() == 0L) {
            Message.send(sender, "<red>Reboot is not scheduled.");
            return;
        }

        MeleeServerManager.getReboot().cancel();
        MeleeServerManager.setReboot(
                new Reboot(null,
                        0L,
                        sender instanceof Player ? ((Player) sender).getUniqueId() : null,
                        System.currentTimeMillis(),
                        0L
                ));

        Message.send(sender, "<green>You've postponed the reboot time by removing it.");
    }

    @Subcommand("postpone")
    @CommandCompletion("@rebooting")
    @CommandPermission("melee.command.reboot|melee.command.reboot.postpone")
    @Syntax("<duration>")
    @Description("Postpones reboot by changing timing")
    public static void onPostpone(CommandSender sender, Duration duration) {
        if (Converter.millisToHours(duration.getDuration() + 1000) > 48) {
            Message.send(sender, "<red>Reboot can only be lower or equal to 48 hours.");
            return;
        }

        MeleeServerManager.getReboot().cancel();
        MeleeServerManager.setReboot(
                new Reboot(
                        sender instanceof Player ? ((Player) sender).getUniqueId() : null,
                        System.currentTimeMillis(),
                        null,
                        0L,
                        (duration.getDuration() + 1000)
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
            Message.send(sender,
                    "<hover:show_text:'<red>Removed by player%"
                            .replace("player%",
                                    MeleeServerManager.getReboot().getRemovedBy() == null ?
                                            "Console" :
                                            Hunter.getHunter(MeleeServerManager.getReboot().getRemovedBy()).getUsernameColored()) +
                            "<newline><yellow>Removed at date%'>"
                                    .replace("date%", Converter.millisToDate(MeleeServerManager.getReboot().getRemovedAt())) +
                    "<red>Instance is not scheduled to reboot. (hover me)"
            );
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
                        "<green>Instance scheduled to reboot in schedule%. (hover me)"
                                .replace("schedule%", Converter.millisToTime(
                                        (MeleeServerManager.getReboot().getAddedAt() + MeleeServerManager.getReboot().getDelay())
                                                - System.currentTimeMillis()))
        );
    }

}
