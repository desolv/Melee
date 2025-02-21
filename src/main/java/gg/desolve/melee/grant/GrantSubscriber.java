package gg.desolve.melee.grant;

import gg.desolve.melee.Melee;
import gg.desolve.mithril.Mithril;
import gg.desolve.mithril.relevance.Message;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GrantSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$");
        UUID uuid = UUID.fromString(parts[0]);
        List<String> scopes = Arrays.asList(parts[1].split(","));
        String msg = parts[2];

        if (!scopes.contains(Mithril.getInstance().getInstanceManager().getInstance().getName())
                && !scopes.contains("global"))
            return;

        Melee.getInstance().getProfileManager().getRecords().remove(uuid);
        Melee.getInstance().getProfileManager().retrieve(uuid);

        Message.send(Bukkit.getPlayer(uuid), msg);
    }
}
