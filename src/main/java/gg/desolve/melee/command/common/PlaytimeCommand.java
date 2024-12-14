package gg.desolve.melee.command.common;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.command.inventory.grant.GrantInventory;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand extends BaseCommand {

    @CommandAlias("playtime")
    @CommandPermission("melee.command.playtime")
    @Description("Check your playtime")
    public static void execute(Player player) {
        Hunter hunter = Hunter.getHunter(player.getUniqueId());

        Message.send(player,
                "<green>Your total playtime is <yellow>playtime% <green>with <light_purple>logins% logins."
                        .replace("player%", hunter.getUsernameColored())
                        .replace("playtime%", Converter.millisToTime(hunter.getPlaytime()))
                        .replace("logins%", String.valueOf(hunter.getLogins()))
        );
    }

    @CommandAlias("playtime")
    @CommandCompletion("@players")
    @CommandPermission("melee.command.playtime.others")
    @Syntax("<player>")
    @Description("Check a player playtime")
    public static void execute(CommandSender sender, Hunter hunter) {
        Message.send(sender,
                "player% <green>total playtime is <yellow>playtime% <green>with <light_purple>logins% logins."
                        .replace("player%", hunter.getUsernameColored())
                        .replace("playtime%", Converter.millisToTime(hunter.getPlaytime()))
                        .replace("logins%", String.valueOf(hunter.getLogins()))
        );

    }

}
