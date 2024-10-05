package gg.desolve.melee.rank;

import gg.desolve.melee.configuration.MeleeConfigManager;
import org.bukkit.plugin.Plugin;

public class MeleeRankManager {

    public MeleeRankManager(Plugin plugin) {
        try {
            MeleeConfigManager.getRankConfig().getConfig().getKeys(false).forEach(Rank::load);
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem loading ranks.");
            e.printStackTrace();
        }
    }

}
