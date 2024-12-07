package gg.desolve.melee.configuration;

import lombok.Getter;
import org.bukkit.plugin.Plugin;

@Getter
public class MeleeConfigManager {

    public static Config storage;
    public static Config rank;
    public static Config acf;
    public static Config lang;

    public MeleeConfigManager(Plugin plugin) {
        try {
            storage = new Config("storage.yml");
            rank = new Config("ranks.yml");
            acf = new Config("acf-lang.yml");
            lang = new Config("language.yml");
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem loading configs.");
            e.printStackTrace();
        }
    }
}
