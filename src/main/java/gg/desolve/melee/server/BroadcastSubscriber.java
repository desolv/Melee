package gg.desolve.melee.server;

import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

@Getter
public class BroadcastSubscriber extends JedisPubSub {

    public static final String update = "broadcastUpdate";

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$", 4);
        String scope = parts[0];
        String type = parts[1]; // command or message check
        String action = parts[2]; // command or message action
        String extra = parts[3]; // permission type ; if global is everyone

        if (scope.equalsIgnoreCase("global") || scope.equalsIgnoreCase(Bukkit.getServerName())) {
            if (type.equalsIgnoreCase("command")) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action);
            } else if (type.equalsIgnoreCase("message")) {
                if (extra.equalsIgnoreCase("global")) {
                    Bukkit.broadcastMessage(Message.translate(action));
                } else {
                    Bukkit.getOnlinePlayers().stream()
                            .filter(player -> Hunter.getHunter(player.getUniqueId()).hasPermission(extra))
                            .forEach(player -> Message.send(player, action));
                }
            }
        }
    }

}
