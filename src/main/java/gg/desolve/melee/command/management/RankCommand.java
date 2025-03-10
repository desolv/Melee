package gg.desolve.melee.command.management;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import gg.desolve.melee.Melee;
import gg.desolve.melee.inventory.rank.metadata.MetadataInventory;
import gg.desolve.melee.rank.Rank;
import gg.desolve.melee.rank.RankManager;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
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
    public static void onCreate(CommandSender sender, @Single String name) {
        RankManager rankManager = Melee.getInstance().getRankManager();
        Rank rank = rankManager.retrieve(name);
        if (rank != null) {
            Message.send(sender, rank.getDisplayColored() + " <red>rank is present.");
            return;
        }

        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();

        Rank newRank = rankManager.create(name);
        newRank.setVisible(false);
        newRank.save();

        Message.send(sender, "<green>Created " + newRank.getDisplayColored() + " <green>rank (check rank metadata).");
    }

    @Subcommand("confirm")
    @CommandCompletion("@ranks")
    @CommandPermission("melee.command.rank|melee.command.rank.confirm")
    @Syntax("(rank)")
    @Description("Confirm pending deletion for a rank")
    public static void onConfirm(CommandSender sender, Rank rank) {
        if (!(sender instanceof ConsoleCommandSender)) {
            Message.send(sender, "<red>Console only command.");
            return;
        }

        if (!rank.isPending() || rank.isPrimary()) {
            Message.send(sender, "<red>Unable to confirm pending deletion for " + rank.getNameColored() + " <red>rank.");
            return;
        }

        String name = rank.getNameColored();
        Melee.getInstance().getRankManager().delete(rank);

        Message.send(sender, "<green>Deleted " + name + " <green>rank.");
        String broadcastMessage = "prefix% rank% <green>rank has been deleted."
                .replace("rank%", name);

        String redisMessage = String.join("&%$",
                broadcastMessage,
                "melee.*|melee.admin"
        );

        Mithril.getInstance().getRedisManager().publish("Broadcast", redisMessage);
    }
}
