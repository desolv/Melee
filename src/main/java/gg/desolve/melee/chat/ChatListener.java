package gg.desolve.melee.chat;

import gg.desolve.melee.Melee;
import gg.desolve.melee.chat.process.RankChatProcessHandler;
import gg.desolve.melee.chat.process.RankCreateChatProcessHandler;
import gg.desolve.melee.profile.Profile;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Profile profile = Melee.getInstance().getProfileManager().retrieve(player.getUniqueId());
        String message = event.getMessage();

        if (profile.getProcess() != null && !profile.getProcess().isEmpty()) {
            event.setCancelled(true);

            String[] parts = profile.getProcess().split(":", 3);
            profile.setProcess("");

            if (parts[0].equalsIgnoreCase("create"))
                new RankCreateChatProcessHandler().process(player, message);
            if (parts[0].equalsIgnoreCase("rank"))
                new RankChatProcessHandler(profile, Melee.getInstance().getRankManager().retrieve(parts[1]), parts[2]).process(player, message);
        }
    }


}
