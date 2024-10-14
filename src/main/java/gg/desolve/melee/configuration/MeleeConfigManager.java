package gg.desolve.melee.configuration;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

@Getter
public class MeleeConfigManager {

    private Config storage;
    private Config rank;
    private Config message;

    public MeleeConfigManager(Plugin plugin) {
        try {
            storage = new Config("storage.yml");
            rank = new Config("ranks.yml");
            message = new Config("messages.yml");
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem loading configs.");
            e.printStackTrace();
        }
    }
}
