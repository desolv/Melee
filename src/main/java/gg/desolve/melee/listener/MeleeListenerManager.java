package gg.desolve.melee.listener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;

public class MeleeListenerManager {

    public MeleeListenerManager(Plugin plugin) {
        try {
            PluginManager pluginManager = Bukkit.getServer().getPluginManager();

            Arrays.asList(
                    new ProfileListener()
            ).forEach(listener -> pluginManager.registerEvents(listener, plugin));
        } catch (Exception ex) {
            plugin.getLogger().warning("There was a problem loading listeners.");
            ex.printStackTrace();
        }
    }

}
