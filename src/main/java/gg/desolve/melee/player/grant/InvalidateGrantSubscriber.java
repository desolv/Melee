package gg.desolve.melee.player.grant;

import gg.desolve.melee.common.Message;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.player.profile.Hunter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

@Getter
public class InvalidateGrantSubscriber extends JedisPubSub {

    public static final String update = "InvalidateGrantSubscriber";

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$", 3);
        String uuid = parts[0];
        String senderUsername = parts[1];
        String msg = parts[2];

        Hunter hunter = Hunter.getHunter(UUID.fromString(uuid));
        Player player = Bukkit.getPlayer(hunter.getUuid());

        if (hunter.getServer().equalsIgnoreCase(MeleeConfigManager.lang.getString("server_name"))) {
            hunter.refreshGrant();
            hunter.refreshPermissions();
            hunter.save();
        }

        if (player != null && (Bukkit.getPlayer(senderUsername) != player)) Message.send(player, msg);
    }

}
