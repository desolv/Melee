package gg.desolve.melee.grant;

import gg.desolve.melee.Melee;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class InvalidateGrantSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$");
        UUID uuid = UUID.fromString(parts[0]);
        String invalidateMessage = parts[1];
        String broadcastMessage = parts[2];

        Melee.getInstance().getProfileManager().getRecords().remove(uuid);
        Melee.getInstance().getProfileManager().retrieve(uuid);

        Message.send(Bukkit.getPlayer(uuid), invalidateMessage);
        Mithril.getInstance().getInstanceManager().broadcast(broadcastMessage + "&%$melee.admin|melee.*");
    }
}
