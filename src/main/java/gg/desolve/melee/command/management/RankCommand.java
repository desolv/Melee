package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.inventory.RankInventory;
import org.bukkit.entity.Player;

@CommandAlias("rank")
public class RankCommand extends BaseCommand {

    @HelpCommand
    @CommandPermission("melee.command.rank|melee.command.rank.help")
    @Syntax("[page]")
    public static void onHelp(CommandHelp help) {
        help.setPerPage(6);
        help.showHelp();
    }

    @Subcommand("list")
    @CommandPermission("melee.command.rank|melee.command.rank.list")
    @Description("Retrieve ranks on GUI")
    public static void onList(Player player) {
        RankInventory.INVENTORY.open(player);
    }

}
