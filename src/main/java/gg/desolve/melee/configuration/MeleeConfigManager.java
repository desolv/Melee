package gg.desolve.melee.configuration;

import gg.desolve.melee.Melee;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

public class MeleeConfigManager {

    @Getter
    private static Config storageConfig;

    @Getter
    private static Config rankConfig;

    @Getter
    private static Config messageConfig;

    public MeleeConfigManager(Plugin plugin) {
        try {
            storageConfig = new Config("storage.yml");
            rankConfig = new Config("ranks.yml");
            messageConfig = new Config("messages.yml");
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem loading configs.");
            e.printStackTrace();
        }
    }
}
