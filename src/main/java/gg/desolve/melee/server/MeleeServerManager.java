package gg.desolve.melee.server;

import com.google.gson.Gson;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class MeleeServerManager {

    @Getter
    private static String id = Converter.generateId();

    public MeleeServerManager(Plugin plugin) {
        if (getServer(id) != null)
            id = Converter.generateId();

        saveServer(
                new Server(
                        id,
                        plugin.getServer().getServerName(),
                        plugin.getServer().getVersion(),
                        Melee.getInstance().getDescription().getVersion(),
                        Bukkit.getOnlinePlayers().size(),
                        Melee.getInstance().getBooting()
                )
        );

        connected();
        startHeartbeat();
    }

    private void startHeartbeat() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Server server = getServers().stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst().get();
                server.setHeartbeat(System.currentTimeMillis());
                server.setOnline(Bukkit.getOnlinePlayers().size());
                saveServer(server);
            }
        }.runTaskTimer(Melee.getInstance(), 0, 20 * 60);
    }

    public static void connected() {
        String redisMessage = String.join("&%$",
                "global",
                "message",
                "<hover:show_text:'<dark_gray>#id%<newline><green>version%'>"
                        .replace("id%", id)
                        .replace("version%", Melee.getInstance().getServer().getVersion()) +
                        "<red>[Admin] <aqua>" + Melee.getInstance().getServer().getServerName() + " <aqua>has <green>connected <aqua>with <green>"
                        + (System.currentTimeMillis() - Melee.getInstance().getBooting()) + "ms.",
                "melee.admin"
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(BroadcastSubscriber.update, redisMessage);
        }
    }

    public static void disconnected(Server server) {
        String redisMessage = String.join("&%$",
                "global",
                "message",
                ("<hover:show_text:'<dark_gray>#id%" +
                        "<newline><green>version%" +
                        "<newline><red>Heartbeat of heartbeat% seconds" +
                        "<newline><light_purple>Instance up for duration%'>")
                        .replace("id%", server.getId())
                        .replace("version%", server.getVersion())
                        .replace("heartbeat%", String.valueOf(Converter.millisToSeconds(System.currentTimeMillis() - server.getHeartbeat())))
                        .replace("duration%", Converter.millisToTime(System.currentTimeMillis() - server.getBooting())) +
                        "<red>[Admin] <aqua>" + Melee.getInstance().getServer().getServerName() + " <aqua>has <red>disconnected.",
                "melee.admin"
        );

        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.publish(BroadcastSubscriber.update, redisMessage);
        }
    }

    public static Server getServer(String id) {
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            String serverJson = jedis.get("server:" + id);
            if (serverJson != null) {
                Server server = new Gson().fromJson(serverJson, Server.class);
                if (Converter.millisToSeconds(System.currentTimeMillis() - server.getHeartbeat()) > 65) {
                    removeServer(server.getId());
                    disconnected(server);
                    return null;
                }
                return server;
            }
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem loading the server from Redis.");
            e.printStackTrace();
        }
        return null;
    }

    public static void saveServer(Server server) {
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            String serverJson = new Gson().toJson(server);
            jedis.set("server:" + server.getId(), serverJson);
            jedis.sadd("servers", server.getId());
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving the server to Redis.");
            e.printStackTrace();
        }
    }

    public static void removeServer(String id) {
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.del("server:" + id);
            jedis.srem("servers", id);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem removing the server from Redis.");
            e.printStackTrace();
        }
    }

    public static List<Server> getServers() {
        List<Server> servers = new ArrayList<>();
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.smembers("servers").forEach(s -> {
                Server server = getServer(s);
                if (server != null) servers.add(server);
            });
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem retrieving all servers from Redis.");
            e.printStackTrace();
        }
        return servers;
    }
}
