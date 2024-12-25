package gg.desolve.melee.player.punishment;

import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

@Getter
public class PunishmentSubscriber extends JedisPubSub {

    public static final String update = "punishmentSubscriber";

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$", 3);
        String uuid = parts[0];
        String scope = parts[1];
        String msg = parts[2];

        Hunter hunter = Hunter.getHunter(UUID.fromString(uuid));
        Player player = Bukkit.getPlayer(hunter.getUuid());

        if ((scope.equalsIgnoreCase("global") || Melee.getInstance().getConfig("language.yml").getString("server_name").equalsIgnoreCase(scope))
                && (!scope.equalsIgnoreCase("global") && hunter.getServer().equals(scope))) {
            hunter.evaluatePunishments();
            hunter.save();

            if (player != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.kickPlayer(Message.translate(msg));
                    }
                }.runTask(Melee.getInstance());
            }
        }
    }

}
