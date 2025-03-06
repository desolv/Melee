package gg.desolve.melee.chat.process;

import gg.desolve.melee.Melee;
import gg.desolve.melee.inventory.rank.RankInventory;
import gg.desolve.melee.rank.Rank;
import gg.desolve.melee.rank.RankManager;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public class RankCreateChatProcessHandler {

    public void process(Player player, String message) {
        Bukkit.getLogger().info(message);
        if (message.equalsIgnoreCase("cancel")) {
            Message.send(player, "<red>Modification process cancelled.");
            return;
        }

        if (message.contains(" ")) {
            Message.send(player, "<red>Name cannot contain spaces.");
            return;
        }

        RankManager rankManager = Melee.getInstance().getRankManager();
        Rank rank = rankManager.retrieve(message);
        if (rank != null) {
            Message.send(player, rank.getDisplayColored() + " <red>rank is present.");
            return;
        }

        message = message.substring(0, 1).toUpperCase() + message.substring(1).toLowerCase();

        Rank newRank = rankManager.create(message);
        newRank.setVisible(false);
        newRank.save();

        Message.send(player, "<green>Created " + message + " <green>rank.");
        RankInventory.INVENTORY.open(player);
    }
}
