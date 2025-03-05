package gg.desolve.melee.rank;

import gg.desolve.melee.Melee;
import gg.desolve.melee.profile.Profile;
import org.bukkit.Bukkit;
import redis.clients.jedis.JedisPubSub;

public class RankSubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$");
        String rank = parts[0];
        String type = parts[1];

        Melee.getInstance().getRankManager().getRecords().remove(rank);

        if (type.equals("refresh"))
            Bukkit.getOnlinePlayers().forEach(player -> {
                Profile profile = Melee.getInstance().getProfileManager().retrieve(player.getUniqueId());
                profile.refreshGrants();
            });
    }
}
