package gg.desolve.melee.player.grant;

import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Message;
import gg.desolve.melee.player.profile.Hunter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPubSub;
@Getter
public class GrantSubscriber extends JedisPubSub {

    public static final String channel = "grantUpdate";

    @Override
    public void onMessage(String channel, String message) {
        String[] parts = message.split("&%\\$", 7);
        String username = parts[0];
        String senderUsername = parts[1];
        long durationValue = Long.parseLong(parts[2]);
        String grantId = parts[3];
        String rankName = parts[4];
        String scope = parts[5];
        String msg = parts[6];

        Hunter hunter = Hunter.getHunter(username);
        Player player = Bukkit.getPlayer(hunter.getUuid());

        if ((scope.equalsIgnoreCase("global") || Bukkit.getServerName().equalsIgnoreCase(scope))
                && hunter.getServer().equals(scope)) {
            hunter.refreshGrant();
            hunter.refreshPermissions();
            hunter.save();

            if (durationValue != Integer.MAX_VALUE && Converter.millisToHours(durationValue) <= 48) {
                Runnable runnable = () -> {
                    if (hunter.getUsername() != null) {
                        hunter.evaluateGrants();
                    }
                };

                hunter.addSchedule(
                        grantId + rankName,
                        runnable,
                        (durationValue + 1000)
                );
            }
        }

        if (player != null && (Bukkit.getPlayer(senderUsername) != player)) Message.send(player, msg);
    }

}
