package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.inventory.rank.metadata.MetadataInventory;
import gg.desolve.melee.inventory.rank.modify.ModifyRankInventory;
import gg.desolve.melee.rank.Rank;
import gg.desolve.melee.rank.RankManager;
import gg.desolve.mithril.relevance.Message;
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
        MetadataInventory.INVENTORY.open(player);
    }

    @Subcommand("create")
    @CommandPermission("melee.command.rank|melee.command.rank.create")
    @Description("Create new rank")
    @Syntax("<name>")
    public static void onCreate(Player player, @Single String name) {
        RankManager rankManager = Melee.getInstance().getRankManager();
        Rank rank = rankManager.retrieve(name);
        if (rank != null) {
            Message.send(player, rank.getDisplayColored() + " <red>rank is present.");
            return;
        }

        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        Rank newRank = rankManager.create(name);
        newRank.setVisible(false);
        newRank.save();

        Message.send(player, "<green>Created " + newRank.getDisplayColored() + " <green>rank (check rank metadata).");
    }



}
