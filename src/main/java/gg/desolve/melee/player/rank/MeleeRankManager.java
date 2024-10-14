package gg.desolve.melee.player.rank;

import gg.desolve.melee.configuration.MeleeConfigManager;
import org.bukkit.plugin.Plugin;

public class MeleeRankManager {

    public MeleeRankManager(Plugin plugin) {
        try {
            new MeleeConfigManager(plugin).getRank().getConfig().getKeys(false)
                    .forEach(Rank::load);
        } catch (Exception e) {
            plugin.getLogger().warning("There was a problem loading ranks.");
            e.printStackTrace();
        }
    }

}
