package gg.desolve.melee.listener;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeleeListenerManager {

    @Getter
    private static final List<String> listeners = new ArrayList<>();

    public MeleeListenerManager(Plugin plugin) {
        try {
            PluginManager pluginManager = Bukkit.getServer().getPluginManager();

            Arrays.asList(
                    new ProfileListener()
            ).forEach(listener ->  {
                pluginManager.registerEvents(listener, plugin);
                listeners.add(listener.getClass().getSimpleName());
            });
        } catch (Exception ex) {
            plugin.getLogger().warning("There was a problem loading listeners.");
            ex.printStackTrace();
        }
    }

}
