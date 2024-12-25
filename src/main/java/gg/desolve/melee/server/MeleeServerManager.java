package gg.desolve.melee.server;

import com.google.gson.Gson;
import gg.desolve.melee.Melee;
import gg.desolve.melee.common.Converter;
import gg.desolve.melee.common.Duration;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class MeleeServerManager {

    @Getter
    private static String id = Converter.generateId().substring(0, 3) +
            Melee.getInstance().getConfig("language.yml").getString("server_name").substring(1, 4) +
            Converter.generateId().substring(6, 10);

    @Getter
    public static String name = Melee.getInstance().getConfig("language.yml").getString("server_name");

    @Getter
    @Setter
    public static Reboot reboot = new Reboot(
            null,
            System.currentTimeMillis(),
            null,
            0L,
            (Duration.fromString("24h").getDuration() + 1000)
    );

    public MeleeServerManager(Plugin plugin) {
        saveServer(new Server(
                        id,
                        Melee.getInstance().getConfig("language.yml").getString("server_name"),
                        plugin.getServer().getVersion(),
                        Melee.getInstance().getDescription().getVersion(),
                        Bukkit.getOnlinePlayers().size(),
                        Melee.getInstance().getBooting()
                ));

        connected();
        startHeartbeat();
        reboot.start();
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
                        "<red>[Admin] <aqua>" + Melee.getInstance().getConfig("language.yml").getString("server_name") + " <aqua>has <green>connected <aqua>with <green>"
                        + (System.currentTimeMillis() - Melee.getInstance().getBooting()) + "ms.",
                "melee.admin",
                "true"
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
                        "<red>[Admin] <aqua>" + Melee.getInstance().getConfig("language.yml").getString("server_name") + " <aqua>has <red>disconnected.",
                "melee.admin",
                "true"
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
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem saving the server to Redis.");
            e.printStackTrace();
        }
    }

    public static void removeServer(String id) {
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.del("server:" + id);
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem removing the server from Redis.");
            e.printStackTrace();
        }
    }

    public static List<Server> getServers() {
        List<Server> servers = new ArrayList<>();
        try (Jedis jedis = Melee.getInstance().getRedisManager().getConnection()) {
            jedis.keys("server:*").forEach(s -> {
                Server server = getServer(s.replace("server:", ""));
                if (server != null)
                    servers.add(server);
            });
        } catch (Exception e) {
            Melee.getInstance().getLogger().warning("There was a problem retrieving all servers from Redis.");
            e.printStackTrace();
        }
        return servers;
    }
}
