package gg.desolve.melee;

import fr.minuskube.inv.InventoryManager;
import gg.desolve.melee.command.MeleeCommandManager;
import gg.desolve.melee.command.inventory.MeleeInventoryManager;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.MeleeRankManager;
import gg.desolve.melee.server.BroadcastSubscriber;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.storage.MeleeMongoManager;
import gg.desolve.melee.storage.redis.MeleeRedisManager;
import gg.desolve.melee.storage.redis.MeleeSubscriberManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Getter
    @Setter
    private MeleeMongoManager mongoManager;

    @Getter
    @Setter
    private MeleeRedisManager redisManager;

    @Getter
    private BukkitAudiences adventure;

    @Getter
    private InventoryManager inventoryManager;

    @Getter
    private boolean isDisabling = false;

    @Getter
    private long booting;

    @Override
    public void onEnable() {
        instance = this;
        booting = System.currentTimeMillis();

        new MeleeConfigManager(this);
        new MeleeMongoManager(this, booting);
        new MeleeRedisManager(this, booting);
        new MeleeServerManager(this);
        new MeleeRankManager(this);
        new MeleeListenerManager(this);
        new MeleeCommandManager(this);
        new MeleeSubscriberManager(this);

        this.adventure = BukkitAudiences.create(this);
        this.inventoryManager = new InventoryManager(this);
        inventoryManager.init();
    }

    @Override
    public void onDisable() {
        isDisabling = true;
        Bukkit.getOnlinePlayers().forEach(player -> {
            try {
                Hunter hunter = Hunter.getHunter(player.getUniqueId());
                hunter.setLastSeen(System.currentTimeMillis());
                hunter.saveMongo();
            } catch (Exception e) {
                instance.getLogger().warning("There was a problem saving " + player.getName() + "' on disable.");
            }
        });
        MeleeServerManager.removeServer(MeleeServerManager.getId());

        String redisMessage = String.join("&%$",
                "global",
                "message",
                "<red>[Admin] <aqua>" + this.getServer().getServerName() + " <aqua>has <red>disconnected.",
                "melee.admin"
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(BroadcastSubscriber.update, redisMessage);
        }

        mongoManager.getMongoClient().close();
        redisManager.getJedisPool().close();
    }

}
