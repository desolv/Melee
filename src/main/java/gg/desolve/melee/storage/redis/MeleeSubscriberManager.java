package gg.desolve.melee.storage.redis;

import gg.desolve.melee.Melee;
import gg.desolve.melee.player.grant.GrantSubscriber;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeleeSubscriberManager {

    @Getter
    private static final List<String> subscribers = new ArrayList<>();

    public MeleeSubscriberManager(Plugin plugin) {
        try {

            Arrays.asList(
                    new GrantSubscriber()
            ).forEach(subscriber -> {
                String channel = "";
                try {
                    channel = (String) subscriber.getClass().getField("channel").get(null);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                subscribe(channel, subscriber);
                subscribers.add(subscriber.getClass().getSimpleName());
            });



        } catch (Exception ex) {
            plugin.getLogger().warning("There was a problem loading subscribers.");
            ex.printStackTrace();
        }
    }

    public void subscribe(String channel, JedisPubSub subscriber) {
        new Thread(() -> {
            try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
                jedis.subscribe(subscriber, channel);
            } catch (Exception e) {
                Melee.getInstance().getLogger().warning("Failed to subscribe to " + channel + ".");
                e.printStackTrace();
            }
        }).start();
    }

}
