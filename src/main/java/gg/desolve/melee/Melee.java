package gg.desolve.melee;

import com.google.gson.Gson;
import fr.minuskube.inv.InventoryManager;
import gg.desolve.commons.Commons;
import gg.desolve.commons.config.Config;
import gg.desolve.commons.config.ConfigurationManager;
import gg.desolve.commons.redis.RedisManager;
import gg.desolve.melee.command.MeleeCommandManager;
import gg.desolve.melee.listener.MeleeListenerManager;
import gg.desolve.melee.player.profile.Hunter;
import gg.desolve.melee.player.rank.MeleeRankManager;
import gg.desolve.melee.server.MeleeServerManager;
import gg.desolve.melee.storage.MeleeMongoManager;
import gg.desolve.melee.storage.redis.MeleeSubscriberManager;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class Melee extends JavaPlugin {

    @Getter
    public static Melee instance;

    @Getter
    public ConfigurationManager configurationManager;

    @Getter
    public RedisManager redisManager;

    @Getter
    public final Gson gson = new Gson();

    @Getter
    @Setter
    private MeleeMongoManager mongoManager;

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

        configurationManager = new ConfigurationManager(this, "language.yml", "storage.yml");
        redisManager = Commons.getInstance().getRedisManager();


        new MeleeMongoManager(this, booting);
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
        Hunter.saveAll();
        MeleeServerManager.removeServer(MeleeServerManager.getId());
        mongoManager.getMongoClient().close();
        redisManager.close();
    }

    public Config getConfig(String name) {
        return configurationManager.getConfig(name);
    }
}
