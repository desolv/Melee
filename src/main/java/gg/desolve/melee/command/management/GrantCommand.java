package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.command.inventory.grant.GrantInventory;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import org.bukkit.entity.Player;

@CommandAlias("grant")
public class GrantCommand extends BaseCommand {

    @Default
    @CommandCompletion("@players")
    @CommandPermission("melee.command.grant|melee.command.grantmanual")
    @Syntax("<player>")
    @Description("Grant to a player")
    public static void execute(Player player, Hunter hunter) {
        Message.send(player, "<green>Granting for " + hunter.getUsernameColored() + "..");
        GrantInventory.getInventory(hunter).open(player);
    }

}
