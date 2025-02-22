package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.inventory.rank.RankInventory;
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

    @Subcommand("metadata")
    @CommandPermission("melee.command.rank|melee.command.rank.metadata")
    @Description("Modify ranks metadata on GUI")
    public static void onMetadata(Player player) {
        RankInventory.INVENTORY.open(player);
    }

}
