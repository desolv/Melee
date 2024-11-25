package gg.desolve.melee;

import gg.desolve.melee.command.MeleeCommandManager;
import gg.desolve.melee.configuration.MeleeConfigManager;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.MeleeRankManager;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.storage.MeleeMongoManager;
import gg.desolve.melee.storage.redis.MeleeRedisManager;
import gg.desolve.melee.storage.redis.MeleeSubscriberManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
    private boolean isDisabling = false;

    @Getter
    private long booting;

    @Override
    public void onEnable() {
        instance = this;
        booting = System.currentTimeMillis();

        this.adventure = BukkitAudiences.create(this);
        new MeleeConfigManager(this);
        new MeleeMongoManager(this, booting);
        new MeleeRedisManager(this, booting);
        new MeleeSubscriberManager(this);
        new MeleeRankManager(this);
        new MeleeListenerManager(this);
        new MeleeCommandManager(this);
        new MeleeServerManager(this);
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
        mongoManager.getMongoClient().close();
        redisManager.getJedisPool().close();
    }

}
