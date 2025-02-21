package gg.desolve.melee.profile;

import gg.desolve.melee.Melee;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class ProfileSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$");
        String uuid = parts[0];

        Melee.getInstance().getProfileManager().getRecords().remove(UUID.fromString(uuid));
    }
}
