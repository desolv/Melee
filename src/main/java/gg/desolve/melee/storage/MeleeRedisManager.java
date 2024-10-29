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
            poolConfig.setMaxTotal(20);
            poolConfig.setBlockWhenExhausted(true);

            jedisPool = new JedisPool(new MeleeConfigManager(plugin).getStorage().getString("redis.url"));

            Melee.getInstance().setRedisManager(this);
            plugin.getLogger().info("Merged Redis @ " + (System.currentTimeMillis() - millis) + "ms.");

        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem connecting to Redis.");
            e.printStackTrace();
        }
    }

    public Jedis getConnection() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("Failed to obtain Redis connection.");
            e.printStackTrace();
        }
        return jedis;
    }


}
