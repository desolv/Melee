package gg.desolve.melee.player.grant;

import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

@Getter
public class InvalidateGrantSubscriber extends JedisPubSub {

    public static final String update = "invalidateGrantUpdate";

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$", 3);
        String username = parts[0];
        String senderUsername = parts[1];
        String msg = parts[2];

        Hunter hunter = Hunter.getHunter(username);
        Player player = Bukkit.getPlayer(hunter.getUuid());

        if (hunter.getServer().equalsIgnoreCase(Bukkit.getServerName())) {
            hunter.refreshGrant();
            hunter.refreshPermissions();
            hunter.save();
        }

        if (player != null && (Bukkit.getPlayer(senderUsername) != player)) Message.send(player, msg);
    }

}
