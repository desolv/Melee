package gg.desolve.melee.command.common;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.command.inventory.grant.GrantInventory;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("playtime")
public class PlaytimeCommand extends BaseCommand {

    @HelpCommand
    @CommandPermission("melee.command.playtime|melee.command.playtime.help")
    @Syntax("[page]")
    @Description("Shows list of commands")
    public static void onHelp(CommandHelp help) {
        help.setPerPage(6);
        help.showHelp();
    }

    @CommandAlias("playtime")
    @CommandPermission("melee.command.playtime|melee.command.playtime.playtime")
    @Description("Check your playtime")
    public static void onPlaytime(Player player) {
        Hunter hunter = Hunter.getHunter(player.getUniqueId());

        Message.send(player,
                "<green>Your total playtime is <yellow>playtime% <green>with <light_purple>logins% logins."
                        .replace("playtime%", Converter.millisToTime(hunter.getPlaytime()))
                        .replace("logins%", String.valueOf(hunter.getLogins()))
        );
    }

    @CommandAlias("playtime")
    @CommandCompletion("@players")
    @CommandPermission("melee.command.playtime|melee.command.playtime.others")
    @Syntax("<player>")
    @Description("Check a player playtime")
    public static void onPlaytime(CommandSender sender, Hunter hunter) {
        Message.send(sender,
                "player% <green>total playtime is <yellow>playtime% <green>with <light_purple>logins% logins."
                        .replace("player%", hunter.getUsernameColored())
                        .replace("playtime%", Converter.millisToTime(hunter.getPlaytime()))
                        .replace("logins%", String.valueOf(hunter.getLogins()))
        );

    }

    @Subcommand("reset")
    @CommandCompletion("@players")
    @CommandPermission("melee.command.playtime|melee.command.reset")
    @Syntax("<player>")
    @Description("Reset a player playtime")
    public static void onReset(CommandSender sender, Hunter hunter) {
        Message.send(sender,
                "<green>You've reset player% playtime from <yellow>playtime% to 0 seconds."
                        .replace("player%", hunter.getUsernameColored())
                        .replace("playtime%", Converter.millisToTime(hunter.getPlaytime()))
        );

        hunter.setPlaytime(0L);
    }

}
