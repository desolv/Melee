package gg.desolve.melee.chat;

import gg.desolve.melee.Melee;
import gg.desolve.melee.inventory.grant.GrantHandler;
import gg.desolve.melee.inventory.metadata.MetadataHandler;
import gg.desolve.melee.profile.Profile;
import gg.desolve.melee.rank.Rank;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatListener implements Listener {


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        Profile profile = Melee.getInstance().getProfileManager().retrieve(player.getUniqueId());

        if (profile.getProcess() != null && !profile.getProcess().isEmpty()) {
            event.setCancelled(true);

            String[] parts = profile.getProcess().split(":");
            profile.setProcess("");

            if (message.equalsIgnoreCase("cancel")) {
                Message.send(player, "<red>Modification process cancelled.");
                return;
            }

            Rank rank = Melee.getInstance().getRankManager().retrieve(parts[2]);

            switch (parts[0]) {
                case "rank":
                    new MetadataHandler(profile, rank, parts[3]).process(player, message);
                    break;
                case "grant":
                    Profile targetProfile = Melee.getInstance().getProfileManager().retrieve(UUID.fromString(parts[1]));
                    new GrantHandler(targetProfile, rank, parts[3], parts[4], parts[5]).process(player, message);
                    break;
            }

        }
    }


}
