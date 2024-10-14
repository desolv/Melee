package gg.desolve.melee.storage;

import gg.desolve.melee.Melee;
import gg.desolve.melee.configuration.MeleeConfigManager;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Getter
public class MeleeRedisManager {

    private JedisPool jedisPool;

    public MeleeRedisManager(Plugin plugin, long millis) {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);

            MeleeConfigManager configManager = new MeleeConfigManager(plugin);

            jedisPool = new JedisPool(
                    poolConfig,
                    configManager.getStorage().getString("redis.host"),
                    configManager.getStorage().getInt("redis.port"),
                    5000,
                    configManager.getStorage().getString("redis.password"),
                    configManager.getStorage().getInt("redis.database")
            );

            Melee.getInstance().setRedisManager(this);
            plugin.getLogger().info("Merged Redis @ " + (System.currentTimeMillis() - millis) + "ms.");
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem connecting to Redis.");
            e.printStackTrace();
        }
    }

    public Jedis getConnection() {
        return jedisPool.getResource();
    }

}
